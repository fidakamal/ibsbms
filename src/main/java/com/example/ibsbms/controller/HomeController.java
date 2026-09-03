package com.example.ibsbms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "dashboard";
    }

    @GetMapping("/shareholders")
    public String shareholders() {
        return "shareholder/shareholder-list";
    }
}
