package org.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Hidden
@Controller
public class FrontendController {
        @GetMapping({"/login", "/registration", "/files", "/files/"})
        public String redirect() {
            return "forward:/index.html";
        }
}