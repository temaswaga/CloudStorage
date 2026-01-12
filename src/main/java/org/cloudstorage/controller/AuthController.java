package org.cloudstorage.controller;

import org.cloudstorage.model.dto.RegistrationRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody RegistrationRequestDto request){
        //logic
        System.out.println(request.password() + "  " + request.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody RegistrationRequestDto request){
        //logic
        System.out.println(request.password() + "  " + request.username());
        return ResponseEntity.ok().build();
    }
}
