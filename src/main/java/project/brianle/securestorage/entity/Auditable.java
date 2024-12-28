package project.brianle.securestorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.AlternativeJdkIdGenerator;
import project.brianle.securestorage.exceptions.CustomException;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public abstract class Auditable {
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;
    private String referenceId = new AlternativeJdkIdGenerator().generateId().toString();
    @NotNull
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    @NotNull
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @NotNull
    @CreatedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforePersist(){
        Long userId = 0L; //RequestContext.getUserId();
        if(userId == null){ throw new CustomException("Cannot persist entity without user ID"); }
        setCreatedBy(userId);
        setCreatedAt(LocalDateTime.now());
        setUpdatedBy(userId);
        setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    public void beforeUpdate(){
        Long userId = 0L; //RequestContext.getUserId();
        if(userId == null){ throw new CustomException("Cannot update entity without user ID"); }
        setUpdatedBy(userId);
        setUpdatedAt(LocalDateTime.now());
    }
}
