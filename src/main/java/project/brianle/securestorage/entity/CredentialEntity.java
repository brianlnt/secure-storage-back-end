package project.brianle.securestorage.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@ToString
@Builder //enable the builder pattern for constructing UserEntity objects
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credentials")
@JsonInclude(JsonInclude.Include.NON_DEFAULT) //ensure that default values (like false for boolean) are not included in the database
public class CredentialEntity extends Auditable{
    private String password;
    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER) //The fetch = FetchType.EAGER means the UserEntity is always loaded when the Credential is loaded.
    @JoinColumn(name = "user_id", nullable = false) //column in the database is named user_id and cannot be null
    @OnDelete(action = OnDeleteAction.CASCADE) //ensure that when a UserEntity is deleted, the corresponding CredentialEntity is also deleted
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") //prevent circular references
    @JsonIdentityReference(alwaysAsId = true) //prevent circular references
    @JsonProperty("user_id") //this field will be serialized as user_id
    private UserEntity userEntity;

    public CredentialEntity(UserEntity userEntity, String password){
        this.userEntity = userEntity;
        this.password = password;
    }
}
