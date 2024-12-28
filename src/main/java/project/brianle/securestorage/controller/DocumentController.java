package project.brianle.securestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.service.DocumentService;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/documents"})
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Response> saveDocument(@AuthenticationPrincipal UserResponse user, @RequestParam("files") List<MultipartFile> documents, HttpServletRequest request) {
        var newDocument = documentService.saveDocuments(user.getUserId(), documents);
        return ResponseEntity.created(URI.create("")).body(getResponse(request, Map.of("documents", newDocument), "Document(s) uploaded.", HttpStatus.CREATED));
    }


}
