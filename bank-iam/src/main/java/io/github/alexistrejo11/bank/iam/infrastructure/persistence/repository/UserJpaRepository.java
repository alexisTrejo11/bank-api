package io.github.alexistrejo11.bank.iam.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

	boolean existsByEmail(String email);

	@Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
	Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String email);

	@Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
	Optional<UserEntity> findByIdWithRolesAndPermissions(@Param("id") UUID id);
}
