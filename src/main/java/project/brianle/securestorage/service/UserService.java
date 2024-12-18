package project.brianle.securestorage.service;

import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.enumeration.Authority;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
    RoleEntity getRoleName(String name);
    void verifyAccount(String key);
}
