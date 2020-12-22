package com.santaSinChristmasSoap.wsdlToTemplate.service

import org.springframework.stereotype.Service
import java.util.*


@Service
class WsdlService {
    var soapEnvelope: String = ""
    var endpoints: MutableList<String> = Collections.emptyList()
    var mergePoint: Int = 0

    fun wsdlToTemplate(wsdl: String): String? {
        soapEnvelope = wsdl

        removeIrrelevantInformation()
        endpoints = createListContainingAllEndpoints()
        while (soapEnvelope.contains("<xs:complexType") or soapEnvelope.contains("</xs:complexType>")) {
            removeComplexTypes("<xs:complexType", "</xs:complexType>")
        }
        while (soapEnvelope.contains("<xsd:complexType") or soapEnvelope.contains("</xs:complexType>")) {
            removeComplexTypes("<xsd:complexType", "</xsd:complexType>")
        }
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

        if (soapEnvelope.indexOf(endTag) < soapEnvelope.indexOf(startTag)) {
            removeTagAndBody(endTag, startTag)
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
            mergePoint = soapEnvelope.indexOf(endTag)
            soapEnvelope = startWsdl + endWsdl
        }
    }

    fun createSoapEnvelopeTags() {
        var openingString = """xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsc="http://edb.com/ws/WSCommon"""

        val index = soapEnvelope.indexOf("""xmlns:wsc="http://edb.com/ws/WSCommon""") + """xmlns:wsc="http://edb.com/ws/WSCommon""".length
        val quotationIndex = soapEnvelope.indexOf("\"", index)
        val version = soapEnvelope.substring(index, quotationIndex + 1)

        openingString += version

    }

    fun extractSpecifiedEndpoint(): String {
        return soapEnvelope
    }


//    fun createHeader(): String {
//
//
//    }

}