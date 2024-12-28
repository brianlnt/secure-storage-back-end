package project.brianle.securestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.request.UpdateDocumentRequest;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.service.DocumentService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/documents"})
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('document:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> saveDocument(@AuthenticationPrincipal UserResponse user, @RequestParam("files") List<MultipartFile> documents, HttpServletRequest request) {
        var newDocument = documentService.saveDocuments(user.getUserId(), documents);
        return ResponseEntity.created(URI.create("")).body(getResponse(request, Map.of("documents", newDocument), "Document(s) uploaded successfully.", HttpStatus.CREATED));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('document:read') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getDocument(@AuthenticationPrincipal UserResponse user, HttpServletRequest request,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "5") int size) {
        var newDocument = documentService.getDocuments(page, size);
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", newDocument), "Document(s) retrieved successfully.", HttpStatus.OK));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('document:read') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> searchDocument(@AuthenticationPrincipal UserResponse user, HttpServletRequest request,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "5") int size,
                                                @RequestParam(value = "name", defaultValue = "5") String name) {
        var newDocument = documentService.getDocuments(page, size, name);
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", newDocument), "Document(s) retrieved successfully.", HttpStatus.OK));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyAuthority('document:read') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getDocument(@AuthenticationPrincipal UserResponse user, @PathVariable("documentId") String documentId, HttpServletRequest request) {
        var newDocument = documentService.getDocumentByDocumentId(documentId);
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", newDocument), "Document retrieved successfully.", HttpStatus.OK));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('document:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updateDocument(@AuthenticationPrincipal UserResponse user, @RequestBody UpdateDocumentRequest updateDocumentRequest, HttpServletRequest request){
        var updateDocument = documentService.updateDocument(updateDocumentRequest.getDocumentId(), updateDocumentRequest.getName(), updateDocumentRequest.getDescription());
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", updateDocument), "Document updated successfully.", HttpStatus.OK));
    }

    @GetMapping("/download/{documentName}")
    @PreAuthorize("hasAnyAuthority('document:read') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Resource> downloadDocument(@AuthenticationPrincipal UserResponse user, @PathVariable("documentName") String documentName) throws IOException {
        var resource = documentService.getResource(documentName);
        var httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", documentName);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(resource.getFile().toPath())))
                .headers(httpHeaders).body(resource);
    }
}
