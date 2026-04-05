package io.github.alexistrejo11.bank.iam.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.RoleEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

	Optional<RoleEntity> findByName(String name);

	List<RoleEntity> findByNameIn(Collection<String> names);
}
