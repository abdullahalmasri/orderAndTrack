package org.plasma.common.utils;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class GeneratedIdCreatedAt implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private final UUID id;
    @Column(name = "createdAt", nullable = false, updatable = false)
    private final long createdAt;

    public GeneratedIdCreatedAt() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
    }

    public GeneratedIdCreatedAt(UUID id, long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedIdCreatedAt that = (GeneratedIdCreatedAt) o;
        return createdAt == that.createdAt && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt);
    }

    @Override
    public String toString() {
        return "GeneratedIdCreatedAt{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}
