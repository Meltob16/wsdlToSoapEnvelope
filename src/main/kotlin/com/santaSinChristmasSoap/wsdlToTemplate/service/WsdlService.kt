package com.santaSinChristmasSoap.wsdlToTemplate.service

import net.minidev.json.JSONObject
import org.springframework.stereotype.Service
import java.util.*


@Service
class WsdlService {
    var wsdlInput: String = ""
    var soapEnvelope: String = ""
    var endpoints: MutableList<String> = Collections.emptyList()
    var mergePoint: Int = 0
    var complexTypes: MutableList<String> = mutableListOf()
    var messagesWithContent: MutableList<String> = mutableListOf()
    var operationsWithContent: MutableList<String> = mutableListOf()
    var simpleTypeNames: MutableList<String> = Collections.emptyList()
    var complexTypeNames: MutableList<String> = Collections.emptyList()
    var allElementsAndTypesMutable: MutableMap<String, String> = mutableMapOf()

    var openingUrns: List<String> = Collections.emptyList()
    var allSchemasWithNamespace: MutableMap<String, String> = mutableMapOf()


    fun operationToSoapTemplate(operation: String): String? {
        //  wsdlInput = operation
        getDocumentInformation()
        createOpeningString()
        createSoapFromMessage(createListOfMessagesWithElementMaps(findOperationInput(operation)))
        return soapEnvelope
    }

    fun returnOperations(wsdl: String): String {
        wsdlInput = wsdl

        endpoints = createDistinctListFromRegexPattern("<wsdl:operation.* name=\"(.*?)\">", 1).toMutableList()
        var responseObject = JSONObject()
        endpoints.forEachIndexed { index, element ->
            responseObject[index.toString()] = element
        }
        // println(responseObject)
        return responseObject.toString()
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

    fun getDocumentInformation() {
        soapEnvelope = ""
        complexTypes = getContentOfTag("<xs:complexType", "</xs:complexType")
        complexTypes.addAll(getContentOfTag("<xsd:complexType", "</xsd:complexType"))
        complexTypes.addAll(getContentOfTag("<complexType", "</complexType"))
        messagesWithContent = getContentOfTag("<wsdl:message", "</wsdl:message>")
        operationsWithContent = getContentOfTag("<wsdl:operation", "</wsdl:operation>")
        createListOfComplexTypeNames()
        createListOfSimpleTypeNames()

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
        //   endpoints.forEach(System.out::println)
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
        var openingString = """<xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsc="http://edb.com/ws/WSCommon"""

        val index = wsdlInput.indexOf("""xmlns="http://edb.com/ws/WSCommon""") + """xmlns="http://edb.com/ws/WSCommon""".length
        val quotationIndex = wsdlInput.indexOf("\"", index)
        val version = wsdlInput.substring(index, quotationIndex + 1)
        openingString += version

        openingUrns = createDistinctListFromRegexPattern("xmlns:sch[0-9]=\"(.*?)[\"]", 1)

        openingUrns.forEachIndexed { index, element ->
            openingString += " xlmns:urn$index=\"$element\"" //TODO match sch number with urn number
        }
        openingString += ">\n<soapenv:Header>"

        soapEnvelope += openingString
        createMapOfSchemasWithNamespace()
    }

    fun createSoapEnvelopeFromComplexType(complexType: String) {
        val startIndex = wsdlInput.indexOf("<xsd:complexType name=$complexType")
        val endIndex = wsdlInput.indexOf("</xsd:complexType>")
    }

    fun getContentOfTag(startTag: String, endTag: String, name: String = ""): MutableList<String> {
        var tempList: MutableList<String> = mutableListOf()

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
                tempList.add(complexType)
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
                tempList.add(complexType)
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
        return tempList
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

    fun createHeaderTags(element: String, tagPrefix: String) {
        soapEnvelope += "<$tagPrefix:$element>$element</$tagPrefix:$element>\n"
    }

    fun createDistinctListFromRegexPattern(startPattern: String, regexTarget: Int): List<String> {
        val regex = Regex(startPattern)
        return regex.findAll(wsdlInput)
                .toList()
                .map { it.groupValues[regexTarget] }
                .distinct()
    }

    fun createDistinctListFromRegexPattern(startPattern: String, regexTarget: Int, searchText: String?): List<String> {
        val regex = Regex(startPattern)
        return regex.findAll(searchText ?: "")
                .toList()
                .map { it.groupValues[regexTarget] }
                .distinct()
    }

    fun createMapFromRegexPattern(startPattern: String, complexTypeText: String?, grouOne: Int = 3, groupTwo: Int = 4): Map<String, String> {
        val regex = Regex(startPattern)
        return regex.findAll(complexTypeText ?: "")
                .toList()
                .map { it.groupValues[grouOne] to it.groupValues[groupTwo] }
                .toMap()
    }

    fun getListOfElementsFromComplexType(complexType: String, headerOrBody: Int) {
        var tagPrefix = "urn"
        var tagNumber = ""
        if (headerOrBody == 0) {
            tagPrefix = "wsc"
        }
        var searchWord = "<xs:complexType name=\"$complexType\""
        var complexTypeText = complexTypes.find { it.contains(searchWord) }  // TODO add for not XS
        if (complexTypeText == null) {
            searchWord = "<xsd:complexType name=\"$complexType\""
            complexTypeText = complexTypes.find { it.contains(searchWord) }
        }
        if (complexTypeText == null) {
            searchWord = "<complexType name=\"$complexType\""
            complexTypeText = complexTypes.find { it.contains(searchWord) }
        }

        var mapOfNameAndType = createMapFromRegexPattern("<(xsd|xs):(element|attribute).* name=\"(.*?)\".* type=\"(.*?)\"", complexTypeText).toMutableMap()
        mapOfNameAndType.putAll(createMapFromRegexPattern("<(element|attribute).* name=\"(.*?)\".* type=\"(.*?)\"", complexTypeText, 2, 3))

        val libSearch = Regex("<(xsd|xs):(element|attribute).* name=\"(.*?)\".* type=\"(.*?)\"")
        mapOfNameAndType.forEach {
            var typeNameWithRemovedColon = ""
            tagNumber = getUrnNumber(it.key)
            if (tagNumber == "-1") tagNumber = "could not find urn / schema"
            if (headerOrBody == 0) {
                tagNumber = ""
            }
            if (it.value.contains(":")) {
                typeNameWithRemovedColon = removeNamePrefix(it.value)
                if (isSimple(typeNameWithRemovedColon)) {
                    soapEnvelope += "<$tagPrefix$tagNumber:${it.key}> simpleType: $typeNameWithRemovedColon </$tagPrefix$tagNumber:${it.key}>\n"

                } else if (isComplex(typeNameWithRemovedColon)) {
                    soapEnvelope += "<$tagPrefix$tagNumber:${it.key}>\n"
                    getListOfElementsFromComplexType(typeNameWithRemovedColon, headerOrBody)
                    soapEnvelope += "</$tagPrefix$tagNumber:${it.key}>\n"
                } else {
                    createHeaderTags(it.key, tagPrefix)
                }
            } else if (isSimple(it.value)) {
                soapEnvelope += "<$tagPrefix$tagNumber:${it.key}> simpleType: ${it.value} </$tagPrefix$tagNumber:${it.key}>\n"
            } else if (isComplex(it.value)) {
                val nameTag = it.key
                soapEnvelope += "<$tagPrefix$tagNumber:$nameTag>\n"
                getListOfElementsFromComplexType(it.value, headerOrBody)
                soapEnvelope += "</$tagPrefix$tagNumber:$nameTag>\n"
            } else {
                createHeaderTags(it.key, tagPrefix + tagNumber)
            }


        }
    }

    fun findOperationInput(operationName: String): String? {
        val operationSearchString = "<wsdl:operation name=\"$operationName\""
        val operation = operationsWithContent.find { it.contains(operationSearchString) }
        val messageName = createDistinctListFromRegexPattern("<wsdl:input(.*?)name=\"(.*?)\"", 2, operation)
        return messageName.first().toString()

    }

    fun createListOfMessagesWithElementMaps(messageName: String?): MutableMap<String, String> {
        allElementsAndTypesMutable = createMapFromRegexPattern("<(xsd|xs):(element|attribute).* name=\"(.*?)\".* type=\"(.*?)\"", wsdlInput).toMutableMap()


        val messageSearchString = "<wsdl:message name=\"$messageName\""
        val message = messagesWithContent.find { it.contains(messageSearchString) }
        val elementsOfMessage = createDistinctListFromRegexPattern("<wsdl:part element=\"(.*?)\"", 1, message)
        val elementsOfMessageNoColon: MutableList<String> = mutableListOf()
        elementsOfMessage.forEach { element ->
            if (element.contains(":")) {
                elementsOfMessageNoColon.add(removeNamePrefix(element))
            }
        }

        allElementsAndTypesMutable.forEach { key, value ->
            if (value.contains(":")) {
                allElementsAndTypesMutable[key] = removeNamePrefix(value)
            }
        }

        val mapOfMessage: MutableMap<String, String> = mutableMapOf()
        elementsOfMessageNoColon.forEach { element ->
            mapOfMessage[element] = allElementsAndTypesMutable[element] ?: ""
        }

        return mapOfMessage
    }

    fun createSoapFromMessage(message: MutableMap<String, String>) {
        var count = 0
        var tagPrefix = "wsc"
        var tagNumber = ""
        message.forEach { key, value ->
            if (count == 1) {
                tagPrefix = "urn"
            }
            tagNumber = getUrnNumber(key)
            if (count == 0) {
                tagNumber = ""
            }
            soapEnvelope += "<$tagPrefix$tagNumber:$key>"
            getListOfElementsFromComplexType(value, count)
            soapEnvelope += "</$tagPrefix$tagNumber:$key>"
            if (count == 0) {
                soapEnvelope += "\n </soapenv:Header>\n <soapenv:Body>"
                count++
            }
        }
        soapEnvelope += "</soapenv:Body>\n </soapenv:Envelope>"
    }

    fun createListOfComplexTypeNames() {
        complexTypeNames = createDistinctListFromRegexPattern("<(xsd|xs):complexType.* name=\"(.*?)\"", 2).toMutableList()
        complexTypeNames.addAll(createDistinctListFromRegexPattern("<complexType.* name=\"(.*?)\"", 1).toMutableList())
    }

    fun createListOfSimpleTypeNames() {
        simpleTypeNames = createDistinctListFromRegexPattern("<(xsd|xs):simpleType.* name=\"(.*?)\"", 2).toMutableList()
        simpleTypeNames.addAll(createDistinctListFromRegexPattern("<simpleType.* name=\"(.*?)\"", 1).toMutableList())

    }

    fun createMapOfSchemasWithNamespace() {
        var listOfSchemas: MutableList<String> = getContentOfTag("<xsd:schema", "</xsd:schema>")
        listOfSchemas.addAll(getContentOfTag("<xs:schema", "</xs:schema>"))
        listOfSchemas.addAll(getContentOfTag("<schema", "</schema>"))

        val regex = Regex("targetNamespace=\"(.*?)\">")

        listOfSchemas.forEach { item ->
            allSchemasWithNamespace[item] = regex.find(item)?.groupValues?.get(1).toString()
        }
        var TempAllSchemasWithNamespace: MutableMap<String, String> = allSchemasWithNamespace

        allSchemasWithNamespace.forEach { (key, value) ->

            TempAllSchemasWithNamespace[key] = openingUrns.indexOf(value).toString()
        }
        allSchemasWithNamespace = TempAllSchemasWithNamespace
    }

    fun getUrnNumber(name: String): String {
        var urnNumber = ""
        allSchemasWithNamespace.forEach { key, value ->
            if (key.contains(name)) {
                urnNumber = value
            }
        }
        return urnNumber
    }


    fun isComplex(type: String): Boolean =
            (complexTypeNames.contains(type))

    fun isSimple(type: String): Boolean =
            (simpleTypeNames.contains(type))

    fun removeNamePrefix(type: String): String {
        val colonIndex = type.indexOf(":")
        return type.removeRange(0, colonIndex + 1)
    }

    fun extractSpecifiedEndpoint(): String {
        return wsdlInput
    }

    fun addUrns() {
        //TODO

    }
}
