package com.santaSinChristmasSoap.wsdlToTemplate.service

import org.springframework.stereotype.Service


@Service
class WsdlService {

    fun wsdlToTemplate(wsdl: String): String? {
        var wsdl = removeIrrelevantInformation(wsdl)
        wsdl = createListContainingAllEndpoints(wsdl)

        return wsdl
    }

    fun removeIrrelevantInformation(wsdl: String): String {
        // <xs:annotation> </xs:annotation>
        var tempWsdl = wsdl
        while (tempWsdl.contains("<xs:annotation>")) {
            var first = tempWsdl.indexOf("<xs:annotation>")
            var ending = tempWsdl.indexOf("</xs:annotation>",
                    first)
            val startWsdl = tempWsdl.substring(0, first)
            val endWsdl = tempWsdl.substring(ending + 16, tempWsdl.length)
            tempWsdl = startWsdl + endWsdl
        }                                                                       //TODO look into regexing these loops

        while (tempWsdl.contains("<xsd:annotation>")) {
            var first = tempWsdl.indexOf("<xsd:annotation>")
            var ending = tempWsdl.indexOf("</xsd:annotation>",
                    first)
            val startWsdl = tempWsdl.substring(0, first)
            val endWsdl = tempWsdl.substring(ending + 17, tempWsdl.length)
            tempWsdl = startWsdl + endWsdl
        }


        tempWsdl = tempWsdl.replace("<xsd:annotation/>", "")
        tempWsdl = tempWsdl.replace("<xs:annotation/>", "")

        return tempWsdl
    }

    fun createListContainingAllEndpoints(wsdl: String): String { // removes operation tags
        var tempWsdl = wsdl
        val endpoints = mutableListOf<String>()

        val firstTag = "<wsdl:operation name=\""
        while (tempWsdl.contains(firstTag)) {
            var startIndex = tempWsdl.indexOf(firstTag) + firstTag.length
            var i = ""
            var lastIndexOfName = startIndex
            while (i != "\"") {
               lastIndexOfName = lastIndexOfName + 1
                i = tempWsdl[lastIndexOfName].toString()
            }
            val name = tempWsdl.substring(startIndex, lastIndexOfName)
            endpoints.add(name)

            val lastTag = "<wsdl:operation/>"
            var tagEnding = tempWsdl.indexOf(lastTag) + lastTag.length
            val startWsdl = tempWsdl.substring(0, tempWsdl.indexOf("<wsdl:operation name=\""))
            val endWsdl = tempWsdl.substring(tagEnding, tempWsdl.length)
            tempWsdl = startWsdl + endWsdl
        }
        endpoints.forEach(System.out::print)
        return tempWsdl
    }


    fun extractSpecifiedEndpoint(wsdl: String): String {
        return wsdl
    }


//    fun createHeader(): String {
//
//
//    }

}