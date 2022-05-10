package com.anmi.grasscutter.modules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("main")
    public String main(Model model) {
        model.addAttribute("title", "自定义");
        model.addAttribute("onContent", "自定义");
        return "main";
    }

    @GetMapping("index")
    public String index() {

        return "index";
    }
}
