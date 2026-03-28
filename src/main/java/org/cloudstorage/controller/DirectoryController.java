package org.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.model.dto.ResourceDto;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.entity.User;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.repository.FileNodeRepository;
import org.cloudstorage.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DirectoryController {

    private final FileNodeRepository fileNodeRepository;
    private final UserRepository userRepository;

    @GetMapping("/directory")
    public List<ResourceDto> list(
            @RequestParam String path,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User owner = userRepository.getReferenceById(userDetails.getId());

        FileNode parent = null;
        if (path != null && !path.isBlank() && !path.equals("/")) {
            // TODO: найти папку по пути — аналогично resolveParentPath в сервисе
        }

        return fileNodeRepository.findByOwnerAndParent(owner, parent)
                .stream()
                .map(ResourceMapper::toDto)
                .toList();
    }
}

