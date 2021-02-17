package com.zto.ts.esbuff.schedule.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hpyang
 * @description
 * @date 2020/8/10
 */
@RestController
@RequestMapping("/test")
public class DemoController {

    @RequestMapping(value = "/check",method = RequestMethod.GET)
    @ResponseBody
    public String getAutoFlowNum(){
        return "ok";
    }
}