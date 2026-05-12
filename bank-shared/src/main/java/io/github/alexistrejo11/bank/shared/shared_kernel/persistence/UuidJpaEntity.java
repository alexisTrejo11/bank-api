package io.github.alexistrejo11.bank.shared.shared_kernel.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Mapped superclass for entities whose table defines only a surrogate
 * {@code id} (no audit timestamps).
 */
@MappedSuperclass
public abstract class UuidJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	protected UUID id;

	protected UuidJpaEntity() {
	}

	public UUID getId() {
		return id;
	}
}
