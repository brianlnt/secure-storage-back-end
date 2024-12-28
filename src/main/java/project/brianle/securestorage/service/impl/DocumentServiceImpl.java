package project.brianle.securestorage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.brianle.securestorage.dto.IDocument;
import project.brianle.securestorage.dto.response.DocumentResponse;
import project.brianle.securestorage.entity.DocumentEntity;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.exceptions.CustomException;
import project.brianle.securestorage.repository.DocumentRepository;
import project.brianle.securestorage.repository.UserRepository;
import project.brianle.securestorage.service.DocumentService;
import project.brianle.securestorage.service.UserService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.util.StringUtils.cleanPath;
import static project.brianle.securestorage.constant.Constants.FILE_STORAGE;
import static project.brianle.securestorage.utils.DocumentUtils.*;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public Page<IDocument> getDocuments(int page, int size) {
        return documentRepository.findDocuments(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Page<IDocument> getDocuments(int page, int size, String name) {
        return documentRepository.findDocumentsByName(name, PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Collection<DocumentResponse> saveDocuments(String userId, List<MultipartFile> documents) {
        List<DocumentResponse> documentResponses = new ArrayList<>();
        UserEntity userEntity = userRepository.findUserByUserId(userId).get();
        var storage = Paths.get(FILE_STORAGE).toAbsolutePath().normalize();
        try {
            for(MultipartFile document : documents) {
                var filename = cleanPath(Objects.requireNonNull(document.getOriginalFilename()));
                if("..".contains(filename)) throw new CustomException(String.format("Invalid file name: %s", filename));
                var documentEntity = DocumentEntity
                        .builder()
                        .documentId(UUID.randomUUID().toString())
                        .name(filename)
                        .owner(userEntity)
                        .extension(getExtension(filename))
                        .uri(getDocumentUri(filename))
                        .formattedSize(byteCountToDisplaySize(document.getSize()))
                        .icon(setIcon(getExtension(filename)))
                        .build();
                var savedDocument = documentRepository.save(documentEntity);
                Files.copy(document.getInputStream(), storage.resolve(filename), REPLACE_EXISTING);
                DocumentResponse newDocument = fromDocumentEntity(savedDocument, userService.getUserById(savedDocument.getOwner().getId()), userService.getUserById(savedDocument.getOwner().getId()));
                documentResponses.add(newDocument);
            }
            return documentResponses;
        } catch (Exception exception) {
            throw new CustomException("Unable to save documents");
        }
    }

    @Override
    public IDocument updateDocument(String documentId, String name, String description) {
        try {
            var documentEntity = getDocumentEntity(documentId);
            var document = Paths.get(FILE_STORAGE).resolve(documentEntity.getName()).toAbsolutePath().normalize();
            Files.move(document, document.resolveSibling(name), REPLACE_EXISTING);
            documentEntity.setName(name);
            documentEntity.setDescription(description);
            documentRepository.save(documentEntity);
            return getDocumentByDocumentId(documentId);
        } catch (Exception exception) {
            throw new CustomException("Unable to update document");
        }
    }

    private DocumentEntity getDocumentEntity(String documentId) {
        return documentRepository.findByDocumentId(documentId).orElseThrow(() -> new CustomException("Document not found"));
    }

    @Override
    public void deleteDocument(String documentId) {

    }

    @Override
    public IDocument getDocumentByDocumentId(String documentId) {
        return documentRepository.findDocumentByDocumentId(documentId).orElseThrow(() -> new CustomException("Document not found"));
    }

    @Override
    public Resource getResource(String documentName) {
        try {
            var file = Paths.get(FILE_STORAGE).toAbsolutePath().normalize().resolve(documentName);
            if(!Files.exists(file)) throw new CustomException("Document not found");
            return new UrlResource(file.toUri());
        } catch (Exception exception) {
            throw new CustomException("Unable to download document");
        }
    }
}
