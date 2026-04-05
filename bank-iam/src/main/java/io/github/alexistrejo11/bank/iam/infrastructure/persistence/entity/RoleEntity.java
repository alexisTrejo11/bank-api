package io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String name;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "role_permissions",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	private Set<PermissionEntity> permissions = new HashSet<>();

	protected RoleEntity() {
	}

	public RoleEntity(UUID id, String name) {
		this.id = id;
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<PermissionEntity> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<PermissionEntity> permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RoleEntity that = (RoleEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
