package project.brianle.securestorage.utils;

import org.springframework.util.AutoPopulatingList;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.exceptions.ApiException;

public class AccountUtils {
    public static void verifyAccountStatus(UserEntity userEntity){
        if(!userEntity.isEnabled()) throw new ApiException("Account is disabled");
        if(!userEntity.isAccountNonExpired()) throw new ApiException("Account is expired");
        if(!userEntity.isAccountNonLocked()) throw new ApiException("Account is locked");
    }
}
