package project.brianle.securestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.brianle.securestorage.entity.ConfirmationEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<ConfirmationEntity, Long> {
    Optional<ConfirmationEntity> findByKey(String key);
    Optional<ConfirmationEntity> findByUserEntiry(UserEntity userEntity);
}
