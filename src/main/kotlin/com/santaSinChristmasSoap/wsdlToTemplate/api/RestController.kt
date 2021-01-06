package com.santaSinChristmasSoap.wsdlToTemplate.api

import com.santaSinChristmasSoap.wsdlToTemplate.service.WsdlService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("wsdlToTemplate")
class RestController(
    @Autowired
    val wsdlService: WsdlService
) {

    @CrossOrigin(origins = ["http://localhost:5000"], allowedHeaders = ["*"])
    @RequestMapping(method = [RequestMethod.PUT], headers = ["Accept=*/*"])
    @ResponseBody
    fun getWsdl(httpEntity: HttpEntity<String>): MutableList<String> {
        println("body: " + httpEntity.body)
        val body = httpEntity.body.toString()

        return wsdlService.returnOperations(body)
    }

    @CrossOrigin(origins = ["http://localhost:5000"], allowedHeaders = ["*"])
    @RequestMapping(value = ["/chooseOperation/{operation}"], method = [RequestMethod.PUT], headers = ["Accept=*/*"])
    @ResponseBody
    fun getOperationTarget(@PathVariable("operation") operation: String): String? {


        return wsdlService.operationToSoapTemplate(operation)
    }

}