package org.cloudstorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {
        @GetMapping({"/login", "/registration", "/files", "/files/"})
        public String redirect() {
            return "forward:/index.html";
        }
}