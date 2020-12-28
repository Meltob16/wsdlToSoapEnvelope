package com.santaSinChristmasSoap.wsdlToTemplate.service

import org.springframework.stereotype.Service
import java.util.*


@Service
class WsdlService {
    var wsdlInput: String = ""
    var soapEnvelope: String = ""
    var endpoints: MutableList<String> = Collections.emptyList()
    var mergePoint: Int = 0
    var complexTypes: MutableList<String> = mutableListOf()
    var complexTypeNames: List<String> = Collections.emptyList()


    fun wsdlToTemplate(wsdl: String): String? {
        wsdlInput = wsdl
        createListOfComplexTypeNames()
//        removeIrrelevantInformation()
//        endpoints = createListContainingAllEndpoints()
//        while (soapEnvelope.contains("<xs:complexType") or soapEnvelope.contains("</xs:complexType>")) {
//            removeComplexTypes("<xs:complexType", "</xs:complexType>")
//        }
//        while (soapEnvelope.contains("<xsd:complexType") or soapEnvelope.contains("</xs:complexType>")) {
//            removeComplexTypes("<xsd:complexType", "</xsd:complexType>")
//        }
        createListContainingAllEndpoints()
        createOpeningString()
        getComplexTypes("<xs:complexType", "</xs:complexType")
        getComplexTypes("<xsd:complexType", "</xsd:complexType")
        getListOfElementsFromComplexType("EDBHeaderType")
        getListOfElementsFromComplexType("ledgerConfirmationRequest")

//        createOpeningString()

        return wsdlInput
    }

    fun removeIrrelevantInformation() {
        while (wsdlInput.contains("<xs:annotation>")) {
            removeTagAndBody("<xs:annotation>", "</xs:annotation>")
        }
        //TODO look into regexing these loops
        while (wsdlInput.contains("<xsd:annotation>")) {
            removeTagAndBody("<xsd:annotation>", "</xsd:annotation>")
        }
        wsdlInput = wsdlInput.replace("<xsd:annotation/>", "")
        wsdlInput = wsdlInput.replace("<xs:annotation/>", "")

        while (wsdlInput.contains("<xs:simpleType")) {
            removeTagAndBody("<xs:simpleType", "</xs:simpleType>")
        }
    }

    fun createListContainingAllEndpoints(): MutableList<String> { // removes operation tags
        val endpoints = mutableListOf<String>()

        val firstTag = "<wsdl:operation name=\""
        while (wsdlInput.contains(firstTag)) {
            var startIndex = wsdlInput.indexOf(firstTag) + firstTag.length
            var i = ""
            var lastIndexOfName = startIndex
            while (i != "\"") {
                lastIndexOfName += 1
                i = wsdlInput[lastIndexOfName].toString()
            }
            val name = wsdlInput.substring(startIndex, lastIndexOfName)
            if (!endpoints.contains(name)) endpoints.add(name)

            removeTagAndBody("<wsdl:operation name=\"", "</wsdl:operation>")
        }
        endpoints.forEach(System.out::println)
        return endpoints
    }

    fun removeTagAndBody(startTag: String, endTag: String, name: String = "") {
        val endTagIndex = wsdlInput.indexOf(endTag) + endTag.length
        val startWsdl = wsdlInput.substring(0, wsdlInput.indexOf(startTag))
        val endWsdl = wsdlInput.substring(endTagIndex, wsdlInput.length)
        wsdlInput = startWsdl + endWsdl
    }

    fun removeComplexTypes(startTag: String, endTag: String, name: String = "") {
        mergePoint = 0

        if (wsdlInput.indexOf(endTag) < wsdlInput.indexOf(startTag)) {
            val startWsdl = wsdlInput.substring(0, mergePoint)
            val endWsdl = wsdlInput.substring(wsdlInput.indexOf(endTag) + endTag.length, wsdlInput.length)
            wsdlInput = startWsdl + endWsdl
        }

        val endTagIndex = wsdlInput.indexOf(endTag) + endTag.length
        if (wsdlInput.contains(startTag)) {
            mergePoint = wsdlInput.indexOf(startTag)
            val startWsdl = wsdlInput.substring(0, wsdlInput.indexOf(startTag))
            val endWsdl = wsdlInput.substring(endTagIndex, wsdlInput.length)
            wsdlInput = startWsdl + endWsdl

        }

        if ((wsdlInput.indexOf(startTag) == -1) && (wsdlInput.indexOf(endTag) > -1)) {
            val startWsdl = wsdlInput.substring(0, mergePoint)
            val endWsdl = wsdlInput.substring(wsdlInput.indexOf(endTag) + endTag.length, wsdlInput.length)
            wsdlInput = startWsdl + endWsdl
        }
    }

    fun createOpeningString() {
        var openingString = """xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsc="http://edb.com/ws/WSCommon"""

        val index = wsdlInput.indexOf("""xmlns="http://edb.com/ws/WSCommon""") + """xmlns="http://edb.com/ws/WSCommon""".length
        val quotationIndex = wsdlInput.indexOf("\"", index)
        val version = wsdlInput.substring(index, quotationIndex + 1)
        openingString += version

        val urns = createDistinctListFromRegexPattern("xmlns:sch[0-9]=\"(.*?)[\"]", 1)

        urns.forEachIndexed { index, element ->
            openingString += " xlmns:urn$index=\"$element\"" //TODO match sch number with urn number
        }
        openingString += ">\n<soapenv:Header>\n<wsc:AutHeader>"

        soapEnvelope += openingString
    }

    fun createSoapEnvelopeFromComplexType(complexType: String) {
        val startIndex = wsdlInput.indexOf("<xsd:complexType name=$complexType")
        val endIndex = wsdlInput.indexOf("</xsd:complexType>")
    }

    fun getComplexTypes(startTag: String, endTag: String, name: String = "") {
        var tempString = wsdlInput
        var numberOfOpenTags = 0
        var previousStartTagIndex = tempString.indexOf(startTag)
        var previousEndTagIndex = tempString.indexOf(endTag)

        while (tempString.contains(endTag)) {
            previousStartTagIndex = tempString.indexOf(startTag)
            previousEndTagIndex = tempString.indexOf(endTag)

            // only one open case
            if (tempString.indexOf(startTag, previousStartTagIndex + 1) > previousEndTagIndex || (tempString.indexOf(startTag, previousStartTagIndex + 1) == -1)) {
                val complexType = tempString.substring(tempString.indexOf(startTag), tempString.indexOf(endTag, previousEndTagIndex) + endTag.length)
                complexTypes.add(complexType)
                tempString = tempString.replace(complexType, "") // cut out complexType string
            }

            // more than one open case
            else if (nextOpenIsBeforeClose(tempString, startTag, previousStartTagIndex, previousEndTagIndex)) {
                while ((tempString.indexOf(startTag, previousStartTagIndex + 1) != -1) and (nextOpenIsBeforeClose(tempString, startTag, previousStartTagIndex, previousEndTagIndex))) {
                    numberOfOpenTags++
                    previousStartTagIndex = tempString.indexOf(startTag, previousStartTagIndex + 1)
                }
                while (numberOfOpenTags != 0) {
                    numberOfOpenTags--
                    previousEndTagIndex = tempString.indexOf(endTag, previousEndTagIndex + 1)
                }

                val complexType = tempString.substring(tempString.indexOf(startTag), tempString.indexOf(endTag, previousEndTagIndex) + endTag.length + 1)
                complexTypes.add(complexType)
                tempString = tempString.replace(complexType, "")
            }
//            val endTagIndex = tempString.indexOf(endTag) + endTag.length
//
//            if (tempString.contains(startTag)) {
//                mergePoint = tempString.indexOf(startTag)
//                val startWsdl = tempString.substring(0, tempString.indexOf(startTag))
//                val endWsdl = tempString.substring(endTagIndex, tempString.length)
//                tempString = startWsdl + endWsdl
//
//            }
//
//            if ((tempString.indexOf(startTag) == -1) && (tempString.indexOf(endTag) > -1)) {
//                val startWsdl = tempString.substring(0, mergePoint)
//                val endWsdl = tempString.substring(tempString.indexOf(endTag) + endTag.length, tempString.length)
//                tempString = startWsdl + endWsdl
//            }
            //}
        }
    }

    private fun nextOpenIsBeforeClose(tempString: String, startTag: String, previousStartTagIndex: Int, previousEndTagIndex: Int) =
        tempString.indexOf(startTag, previousStartTagIndex + 1) < previousEndTagIndex

//    fun createComplexTypes() { // TODO make list of all complex types
//        //make list of all complex type names
//        val complexTypes: MutableList<String> = Collections.emptyList()
//        val complexTypeNames = createListFromRegexPattern("<(xsd|xs):complexType name=\"(.*?)\"")
//        complexTypeNames.forEach { complexTypes.add(createListFromRegexPattern("<xsd:complexType name=\"(.*)</xsd:complexType>")[0]) }
//
//
//        val complexTypes = createListFromRegexPattern("<xsd:complexType name=\"(.*)</xsd:complexType>")
//        //rekkursjon for å se om de er nøstet?
//        //if list contains <complexTypes.. kall på metoden på nytt på bare den strengen, append til listen.
//        //for each name in list, make list<String> of entire complex types.
//        startIndex = soapEnvelope.indexOf("<xsd:complexType name=")
//
//        // hvis vi har liste med alle complextypes
//        // så kan man hente type på alle elementer
//
//    }

    fun createHeaderTags(element: String) {
        soapEnvelope += "<wsc:$element>$element</wsc:$element>\n"
    }

    fun createDistinctListFromRegexPattern(startPattern: String, regexTarget: Int): List<String> {
        val regex = Regex(startPattern)
        return regex.findAll(wsdlInput)
            .toList()
            .map { it.groupValues[regexTarget] }
            .distinct()
    }

    fun createMapFromRegexPattern(startPattern: String, complexTypeText: String?): Map<String, String> {
        val regex = Regex(startPattern)
        return regex.findAll(complexTypeText ?: "")
            .toList()
            .map { it.groupValues[3] to it.groupValues[4] }
            .toMap()
    }

    fun getListOfElementsFromComplexType(complexType: String) {
        var searchWord = "<xs:complexType name=\"$complexType\""
        var complexTypeText = complexTypes.find { it.contains(searchWord) }
        if(complexTypeText == null) {
            searchWord = "<xsd:complexType name=\"$complexType\""
            complexTypeText = complexTypes.find { it.contains(searchWord) }
        }

        val mapOfNameAndType = createMapFromRegexPattern("<(xsd|xs):(element|attribute).* name=\"(.*?)\".* type=\"(.*?)\"", complexTypeText)
        mapOfNameAndType.forEach {
            if (isComplex(it.value)) {
                val nameTag = it.value
                soapEnvelope += "<wsc:$nameTag>\n"
                getListOfElementsFromComplexType(it.value)
                soapEnvelope += "</wsc:$nameTag>\n"
            } else {
                createHeaderTags(it.key)
            }
        }
//TODO add these </wsc:AutHeader>
//    </soapenv:Header>
//    <soapenv:Body>   before next section

        println(soapEnvelope)
    }

    fun createListOfComplexTypeNames() {
        complexTypeNames = createDistinctListFromRegexPattern("<(xsd|xs):complexType.* name=\"(.*?)\"", 2)
    }

    fun isComplex(type: String): Boolean =
        (complexTypeNames.contains(type))

    fun extractSpecifiedEndpoint(): String {
        return wsdlInput
    }

    fun addUrns(){
        //TODO

    }
}
