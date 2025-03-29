package com.flagcamp.TripPlanner.controller;

import com.flagcamp.TripPlanner.model.RegisterBody;
import com.flagcamp.TripPlanner.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void signUp(@RequestBody RegisterBody request) {
        userService.signUp(
                request.email(),
                request.password()
        );
    }
}
