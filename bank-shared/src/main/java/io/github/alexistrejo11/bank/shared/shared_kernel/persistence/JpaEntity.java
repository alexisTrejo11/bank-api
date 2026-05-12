package io.github.alexistrejo11.bank.shared.shared_kernel.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class JpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	protected UUID id;

	@Column(name = "created_at", nullable = false, updatable = false)
	protected Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	protected Instant updatedAt;

	protected JpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
