package com.flagcamp.TripPlanner.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name="users")
public record UserEntity(
        @Id Long id,
        String email,
        boolean enabled,
        String password
) {
}
