package org.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.service.FileNodeService;
import org.cloudstorage.exception.GlobalExceptionHandler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directory Management", description = "Управление папками: просмотр содержимого и создание новых директорий")
@RestController
@RequiredArgsConstructor
@RequestMapping("/directory")
public class DirectoryController {

    private final FileNodeService fileNodeService;

    @Operation(
            summary = "Получить список файлов и папок",
            description = "Возвращает список всех ресурсов внутри указанной директории. Путь должен заканчиваться на /"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ресурсов успешно получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class)))),
            @ApiResponse(responseCode = "400", description = "Невалидный путь (например, отсутствует / в конце)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping
    public List<ResourceDto> list(
            @Parameter(description = "Путь к директории (пустая строка для корня)", example = "documents/work/")
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validateDirectoryPath(path);

        return fileNodeService.listDirectory(path, userDetails.getId())
                .stream()
                .map(ResourceMapper::toDto)
                .toList();
    }

    @Operation(
            summary = "Создать новую директорию",
            description = "Создает пустую папку по указанному пути. Путь должен заканчиваться на /"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Папка успешно создана",
                    content = @Content(schema = @Schema(implementation = ResourceDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный путь"),
            @ApiResponse(responseCode = "409", description = "Папка с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceDto createDirectory(
            @Parameter(description = "Путь создаваемой директории", example = "images/vacation/")
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validateDirectoryPath(path);

        FileNode newDirectory = fileNodeService.createDirectory(path, userDetails.getId());
        return ResourceMapper.toDto(newDirectory);
    }

    private void validateDirectoryPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (!path.isEmpty() && !path.endsWith("/")) {
            throw new IllegalArgumentException("Directory path must end with '/'");
        }
    }
}