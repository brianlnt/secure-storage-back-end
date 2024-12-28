package project.brianle.securestorage.utils;

import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.exceptions.CustomException;

public class AccountUtils {
    public static void verifyAccountStatus(UserEntity userEntity){
        if(!userEntity.isEnabled()) throw new CustomException("Account is disabled");
        if(!userEntity.isAccountNonExpired()) throw new CustomException("Account is expired");
        if(!userEntity.isAccountNonLocked()) throw new CustomException("Account is locked");
    }
}
