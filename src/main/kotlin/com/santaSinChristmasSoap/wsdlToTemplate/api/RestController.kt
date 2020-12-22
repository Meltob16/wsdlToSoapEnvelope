package com.santaSinChristmasSoap.wsdlToTemplate.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.util.JSONPObject
import com.santaSinChristmasSoap.wsdlToTemplate.service.WsdlService
import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("wsdlToTemplate")
class RestController(
        @Autowired
        val wsdlService: WsdlService
) {


    @RequestMapping(method = [RequestMethod.GET], headers = ["Accept=*/*"])
    @ResponseBody
    fun getWsdl(httpEntity: HttpEntity<String>): String? {
        val body = httpEntity.body

        return wsdlService.wsdlToTemplate(body.toString())
    }

}