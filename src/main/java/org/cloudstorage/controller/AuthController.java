package org.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.AuthRequestDto;
import org.cloudstorage.dto.UserResponseDto;
import org.cloudstorage.exception.GlobalExceptionHandler.ErrorResponse;
import org.cloudstorage.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Управление регистрацией, входом и выходом из системы")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя", description = "Создает аккаунт и сразу авторизует пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sign-up")
    public UserResponseDto register(
            @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.register(request);
        authService.authenticateAndCreateSession(request.username(), request.password(), httpRequest, httpResponse);
        return new UserResponseDto(request.username());
    }

    @Operation(summary = "Вход в систему", description = "Проверяет учетные данные и создает сессию в Redis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sign-in")
    public UserResponseDto login(
            @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.authenticateAndCreateSession(request.username(), request.password(), httpRequest, httpResponse);
        return new UserResponseDto(request.username());
    }

    @Operation(summary = "Выход из системы", description = "Инвалидирует сессию в Redis и очищает Cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешный выход"),
            @ApiResponse(responseCode = "401", description = "Пользователь не был авторизован")
    })
    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Not authenticated");
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        response.setHeader("Set-Cookie", "SESSION=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");
    }
}