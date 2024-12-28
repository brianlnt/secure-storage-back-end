package project.brianle.securestorage.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder //enable the builder pattern for constructing UserEntity objects
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
@JsonInclude(JsonInclude.Include.NON_DEFAULT) //ensure that default values (like false for boolean) are not included in the database
public class DocumentEntity extends Auditable{
    @Column(updatable = false, unique = true, nullable = false)
    private String documentId;
    private String name;
    private String description;
    private String uri;
    private long size;
    private String formattedSize;
    private String icon;
    private String extension;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_documents_owner", foreignKeyDefinition = "foreign key /* FK */ (user_id) references users", value = ConstraintMode.CONSTRAINT)
    )
    private UserEntity owner;
}