package org.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.cloudstorage.dto.UserResponseDto;
import org.cloudstorage.model.security.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal UserDetails userDetails) {
        return new UserResponseDto(userDetails.getUsername());
    }
}