package project.brianle.securestorage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.brianle.securestorage.dto.IDocument;
import project.brianle.securestorage.entity.DocumentEntity;

import static project.brianle.securestorage.constant.Constants.SELECT_DOCUMENTS_QUERY;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    @Query(countQuery = "SELECT COUNT(*) FROM documents", value = SELECT_DOCUMENTS_QUERY, nativeQuery = true)
    Page<IDocument> findDocuments(Pageable pageable);
}
