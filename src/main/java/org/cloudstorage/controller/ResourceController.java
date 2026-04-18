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
import org.cloudstorage.service.StorageService;
import org.cloudstorage.exception.GlobalExceptionHandler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@Tag(name = "Resource Management", description = "Операции с файлами: получение инфо, загрузка, скачивание, удаление и перемещение")
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource")
public class ResourceController {

    private final StorageService storageService;
    private final FileNodeService fileNodeService;

    @Operation(summary = "Получить информацию о ресурсе", description = "Возвращает метаданные файла или папки по указанному пути")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация получена"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResourceDto getResource(
            @Parameter(description = "Полный путь к ресурсу", example = "folder/file.txt") @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);
        FileNode node = fileNodeService.getResource(path, userDetails.getId());
        return ResourceMapper.toDto(node);
    }

    @Operation(summary = "Загрузить файлы", description = "Загружает один или несколько файлов в указанную директорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Файлы успешно загружены",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class)))),
            @ApiResponse(responseCode = "409", description = "Файл с таким именем уже существует")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceDto>> upload(
            @Parameter(description = "Путь к папке загрузки", example = "uploads/") @RequestParam String path,
            @Parameter(description = "Список файлов для загрузки") @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);

        List<ResourceDto> uploadedResources = files.stream()
                .map(file -> storageService.uploadFile(file, path, userDetails.getId()))
                .map(ResourceMapper::toDto)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedResources);
    }

    @Operation(summary = "Скачать ресурс", description = "Скачивает файл напрямую или папку в виде ZIP-архива")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бинарный поток файла/архива",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    })
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> download(
            @Parameter(description = "Путь к скачиваемому ресурсу") @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        validatePath(path);
        return storageService.downloadResource(path, userDetails.getId());
    }

    @Operation(summary = "Удалить ресурс", description = "Безвозвратно удаляет файл или папку (вместе с содержимым)")
    @ApiResponse(responseCode = "204", description = "Успешно удалено")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);
        fileNodeService.deleteResource(path, userDetails.getId());
    }

    @Operation(summary = "Переместить или переименовать ресурс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно перемещено"),
            @ApiResponse(responseCode = "409", description = "Целевой путь уже занят")
    })
    @GetMapping("/move")
    public ResourceDto move(
            @Parameter(description = "Текущий путь") @RequestParam("from") String from,
            @Parameter(description = "Новый путь") @RequestParam("to") String to,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return fileNodeService.moveResource(from, to, userDetails.getId());
    }

    @Operation(summary = "Поиск ресурсов", description = "Ищет файлы и папки по вхождению строки в название (регистронезависимо)")
    @GetMapping("/search")
    public List<ResourceDto> search(
            @Parameter(description = "Поисковый запрос", example = "report") @RequestParam("query") String query,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return fileNodeService.search(query, userDetails.getId());
    }

    private void validatePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
    }
}