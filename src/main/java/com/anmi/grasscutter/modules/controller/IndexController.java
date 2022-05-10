package com.anmi.grasscutter.modules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    @GetMapping("main")
    public ModelAndView main() {
        return new  ModelAndView("main");
    }
}
