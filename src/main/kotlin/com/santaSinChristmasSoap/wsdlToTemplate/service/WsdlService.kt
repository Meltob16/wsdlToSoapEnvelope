package com.santaSinChristmasSoap.wsdlToTemplate.service

import org.springframework.stereotype.Service

@Service
class WsdlService {
    fun wsdlToTemplate(wsdl: String): String? {
        var wsdl = removeIrrelevantInformation(wsdl)

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
        }
        return tempWsdl
    }

//    fun createHeader(): String {
//
//
//    }

}