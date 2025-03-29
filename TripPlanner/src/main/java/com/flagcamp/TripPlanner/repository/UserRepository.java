package com.flagcamp.TripPlanner.repository;

import com.flagcamp.TripPlanner.entity.UserEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface UserRepository extends ListCrudRepository<UserEntity, Long> {
    List<UserEntity> findByFirstName(String firstName);

    List<UserEntity> findByLastName(String lastName);

    UserEntity findByEmail(String email);

    @Modifying
    @Query("UPDATE users SET first_name = :firstName, last_name = :lastName WHERE email = :email")
    void updateNameByEmail(String email, String firstName, String lastName);
}
