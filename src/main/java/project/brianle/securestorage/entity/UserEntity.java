package project.brianle.securestorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder //enable the builder pattern for constructing UserEntity objects
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_DEFAULT) //ensure that default values (like false for boolean) are not included in the database
public class UserEntity extends Auditable{
    @Column(updatable = false, unique = true, nullable = false)
    private String userId;
    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    private Integer loginAttempts;
    private LocalDateTime lastLogin;
    private String phone;
    private String bio;
    private String imageUrl;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean enabled;
    private boolean mfa;
    @JsonIgnore
    private String qrCodeSecret;
    @Column(columnDefinition = "text") //because by default, VARCHAR columns are limited in size (255) use "TEXT" for larger storage
    private String qrCodeImageUri;
    @ManyToOne
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
                    inverseJoinColumns = @JoinColumn(
                            name = "role_id", referencedColumnName = "id"))
    /*
    This defines a many-to-one relationship between UserEntity and RoleEntity.
    Many Users can have the same Role (e.g., "admin" or "user").
    The user_roles table is an intermediate join table that connects users to roles.
    It connects the id from the users table to the id of the roles table.
     */
    private RoleEntity role;
}
