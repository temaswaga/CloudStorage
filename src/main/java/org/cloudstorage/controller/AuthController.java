package org.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.AuthRequestDto;
import org.cloudstorage.dto.UserResponseDto;
import org.cloudstorage.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(
            @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.register(request);

        authService.authenticateAndCreateSession(
                request.username(),
                request.password(),
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(new UserResponseDto(request.username()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(
            @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            authService.authenticateAndCreateSession(
                    request.username(),
                    request.password(),
                    httpRequest,
                    httpResponse
            );

            return ResponseEntity.ok(new UserResponseDto(request.username()));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(new Error("Неверные данные (такого пользователя нет, или пароль неправильный)"));
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        response.setHeader("Set-Cookie", "SESSION=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");

        return ResponseEntity.status(204).build();
    }
}
