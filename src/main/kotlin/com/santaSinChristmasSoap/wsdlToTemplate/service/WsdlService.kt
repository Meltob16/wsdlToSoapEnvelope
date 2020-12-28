package com.santaSinChristmasSoap.wsdlToTemplate.service

import com.sun.el.parser.AstFalse
import org.springframework.stereotype.Service
import java.util.*


@Service
class WsdlService {
    var soapEnvelope: String = ""
    var endpoints: MutableList<String> = Collections.emptyList()
    var mergePoint: Int = 0
    var complexTypes: MutableList<String> = mutableListOf()

    fun wsdlToTemplate(wsdl: String): String? {
        soapEnvelope = wsdl
        createListOfComplexTypeNames()
//        removeIrrelevantInformation()
//        endpoints = createListContainingAllEndpoints()
//        while (soapEnvelope.contains("<xs:complexType") or soapEnvelope.contains("</xs:complexType>")) {
//            removeComplexTypes("<xs:complexType", "</xs:complexType>")
//        }
//        while (soapEnvelope.contains("<xsd:complexType") or soapEnvelope.contains("</xs:complexType>")) {
//            removeComplexTypes("<xsd:complexType", "</xsd:complexType>")
//        }
        getComplexTypes("<xs:complexType", "</xs:complexType")
        getComplexTypes("<xsd:complexType", "</xsd:complexType")
        getListOfElementsFromComplexType("EDBHeaderType")

//        createOpeningString()

        return soapEnvelope
    }

    fun removeIrrelevantInformation() {
        while (soapEnvelope.contains("<xs:annotation>")) {
            removeTagAndBody("<xs:annotation>", "</xs:annotation>")
        }
        //TODO look into regexing these loops
        while (soapEnvelope.contains("<xsd:annotation>")) {
            removeTagAndBody("<xsd:annotation>", "</xsd:annotation>")
        }
        soapEnvelope = soapEnvelope.replace("<xsd:annotation/>", "")
        soapEnvelope = soapEnvelope.replace("<xs:annotation/>", "")

        while (soapEnvelope.contains("<xs:simpleType")) {
            removeTagAndBody("<xs:simpleType", "</xs:simpleType>")
        }
    }

    fun createListContainingAllEndpoints(): MutableList<String> { // removes operation tags
        val endpoints = mutableListOf<String>()

        val firstTag = "<wsdl:operation name=\""
        while (soapEnvelope.contains(firstTag)) {
            var startIndex = soapEnvelope.indexOf(firstTag) + firstTag.length
            var i = ""
            var lastIndexOfName = startIndex
            while (i != "\"") {
                lastIndexOfName += 1
                i = soapEnvelope[lastIndexOfName].toString()
            }
            val name = soapEnvelope.substring(startIndex, lastIndexOfName)
            if (!endpoints.contains(name)) endpoints.add(name)

            removeTagAndBody("<wsdl:operation name=\"", "</wsdl:operation>")
        }
        endpoints.forEach(System.out::println)
        return endpoints
    }

    fun removeTagAndBody(startTag: String, endTag: String, name: String = "") {
        val endTagIndex = soapEnvelope.indexOf(endTag) + endTag.length
        val startWsdl = soapEnvelope.substring(0, soapEnvelope.indexOf(startTag))
        val endWsdl = soapEnvelope.substring(endTagIndex, soapEnvelope.length)
        soapEnvelope = startWsdl + endWsdl
    }

    fun removeComplexTypes(startTag: String, endTag: String, name: String = "") {
        mergePoint = 0

        if (soapEnvelope.indexOf(endTag) < soapEnvelope.indexOf(startTag)) {
            val startWsdl = soapEnvelope.substring(0, mergePoint)
            val endWsdl = soapEnvelope.substring(soapEnvelope.indexOf(endTag) + endTag.length, soapEnvelope.length)
            soapEnvelope = startWsdl + endWsdl
        }

        val endTagIndex = soapEnvelope.indexOf(endTag) + endTag.length
        if (soapEnvelope.contains(startTag)) {
            mergePoint = soapEnvelope.indexOf(startTag)
            val startWsdl = soapEnvelope.substring(0, soapEnvelope.indexOf(startTag))
            val endWsdl = soapEnvelope.substring(endTagIndex, soapEnvelope.length)
            soapEnvelope = startWsdl + endWsdl

        }

        if ((soapEnvelope.indexOf(startTag) == -1) && (soapEnvelope.indexOf(endTag) > -1)) {
            val startWsdl = soapEnvelope.substring(0, mergePoint)
            val endWsdl = soapEnvelope.substring(soapEnvelope.indexOf(endTag) + endTag.length, soapEnvelope.length)
            soapEnvelope = startWsdl + endWsdl
        }
    }

    fun createOpeningString() {
        var openingString = """xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsc="http://edb.com/ws/WSCommon"""

        val index = soapEnvelope.indexOf("""xmlns="http://edb.com/ws/WSCommon""") + """xmlns="http://edb.com/ws/WSCommon""".length
        val quotationIndex = soapEnvelope.indexOf("\"", index)
        val version = soapEnvelope.substring(index, quotationIndex + 1)
        openingString += version

        val urns = createListFromRegexPattern("xmlns:sch[0-9]=\"(.*?)[\"]", 1)

        urns.forEachIndexed { index, element ->
            openingString += " xlmns:urn$index=\"$element\"" //TODO match sch number with urn number
        }
        openingString += ">\n<soapenv:Header>\n<wsc:AutHeader>"

        println(openingString)
    }

    fun createSoapEnvelopeFromComplexType(complexType: String) {
        val startIndex = soapEnvelope.indexOf("<xsd:complexType name=$complexType")
        val endIndex = soapEnvelope.indexOf("</xsd:complexType>")
    }

    fun getComplexTypes(startTag: String, endTag: String, name: String = "") {
        var tempString = soapEnvelope
        var numberOfOpenTags = 0
        var previousStartTagIndex = tempString.indexOf(startTag)
        var previousEndTagIndex = tempString.indexOf(endTag)

        while (tempString.contains(endTag)) {
            previousStartTagIndex = tempString.indexOf(startTag)
            previousEndTagIndex = tempString.indexOf(endTag)

            // only one open case
            if(tempString.indexOf(startTag, previousStartTagIndex + 1) > previousEndTagIndex || (tempString.indexOf(startTag, previousStartTagIndex + 1) == -1)) {
                val complexType = tempString.substring(tempString.indexOf(startTag), tempString.indexOf(endTag, previousEndTagIndex) + endTag.length)
                complexTypes.add(complexType)
                tempString = tempString.replace(complexType, "") // cut out complexType string
            }

            // more than one open case
            else if (nextOpenIsBeforeClose(tempString, startTag, previousStartTagIndex, previousEndTagIndex)) {
                while((tempString.indexOf(startTag, previousStartTagIndex + 1) != -1) and (nextOpenIsBeforeClose(tempString, startTag, previousStartTagIndex, previousEndTagIndex))) {
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

//    fun createHeaderTags(element: String): String {
//        // TODO ta imot et helt element, trekk ut navnet.lag tag
//
//        val startOfElementIndex = soapEnvelope.indexOf(endTag) + endTag.length
//
//
//        val startWsdl = soapEnvelope.substring(0, soapEnvelope.indexOf(startTag))
//        val endWsdl = soapEnvelope.substring(endTagIndex, soapEnvelope.length)
//
//        return "<wsc:$element>\${$element}</wwsc:$element>"
//
//
//        // TODO sjekke typen til et element, hvis det er complextype, kjøre metopde for å lage soap mal av complextype
//    }

    fun createListFromRegexPattern(startPattern: String, regexTarget: Int): List<String> {
        val regex = Regex(startPattern)
        return regex.findAll(soapEnvelope)
            .toList()
            .map { it.groupValues[regexTarget] }
            .distinct()
    }

    fun getListOfElementsFromComplexType(complexType: String) {
        val test = complexTypes.find { it.contains(complexType) }
        println(test)
        val list = createListFromRegexPattern("<xsd:element name=\"(.*?)\" type=\"(.*?)\"", 1)

        val mapOfNameAndType = mapOf<>()
        list.forEach(System.out::println)
    }

    fun createListOfComplexTypeNames() {
        val list = createListFromRegexPattern("<(xsd|xs):complexType.* name=\"(.*?)\"", 2)

        list.forEach(System.out::println)
    }

    fun extractSpecifiedEndpoint(): String {
        return soapEnvelope
    }
}
