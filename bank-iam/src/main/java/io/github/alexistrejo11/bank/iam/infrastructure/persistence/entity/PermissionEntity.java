package io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.UuidJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class PermissionEntity extends UuidJpaEntity {

	@Column(nullable = false, unique = true)
	private String name;

	protected PermissionEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private String name;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public PermissionEntity build() {
			PermissionEntity e = new PermissionEntity();
			e.id = id;
			e.name = name;
			return e;
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PermissionEntity that = (PermissionEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
