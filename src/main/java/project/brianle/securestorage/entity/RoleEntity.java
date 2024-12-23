package project.brianle.securestorage.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import project.brianle.securestorage.enumeration.Authority;

@Getter
@Setter
@ToString
@Builder //enable the builder pattern for constructing UserEntity objects
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@JsonInclude(JsonInclude.Include.NON_DEFAULT) //ensure that default values (like false for boolean) are not included in the database
public class RoleEntity extends Auditable{
    private String name;
    private Authority authorities;
}
