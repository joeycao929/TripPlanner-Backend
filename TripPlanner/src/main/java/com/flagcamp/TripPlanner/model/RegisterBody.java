package com.flagcamp.TripPlanner.model;

public record RegisterBody(
        String email,
        String password,
        String firstName,
        String lastName
) {
}
