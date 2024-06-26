package org.humber.project.controllers;

import org.humber.project.domain.User;
import org.humber.project.domain.UserLoginRequest;
import org.humber.project.exceptions.UserValidationException;
import org.humber.project.services.UserService;
import org.humber.project.services.UserJPAService;
import org.humber.project.services.UserRegistrationValidationService;
import org.humber.project.services.UserLoginValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserJPAService userJPAService;
    private final List<UserRegistrationValidationService> UserRegistrationValidationService;
    private final List<UserLoginValidationService> UserLoginValidationService;

    public UserController(UserService userService, UserJPAService userJPAService, List<UserRegistrationValidationService> userRegistrationValidationService, List<UserLoginValidationService> userLoginValidationService) {
        this.userService = userService;
        this.userJPAService = userJPAService;
        this.UserRegistrationValidationService = userRegistrationValidationService;
        this.UserLoginValidationService = userLoginValidationService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            // Validate the user creation
            for (UserRegistrationValidationService validationService : UserRegistrationValidationService) {
                validationService.validateUserRegistration(user);
            }

            // Hash the user's password before saving it to the database
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            // Replace the plain text password with the hashed password
            user.setPassword(hashedPassword);

            // Proceed to create user if validation passes
            User createdUser = userService.createUser(user);

            if (createdUser != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully. User ID: " + createdUser.getUserId());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user.");
            }
        } catch (UserValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        try {
            // Validate user login credentials
            for (UserLoginValidationService validationService : UserLoginValidationService) {
                validationService.validateUserLogin(userLoginRequest.getUsername(), userLoginRequest.getPassword());
            }

            // Retrieve the user from the database based on the provided username
            User user = userJPAService.getUserByUsername(userLoginRequest.getUsername());

            // Check if the user exists
            if (user != null) {
                // Compare the hashed password from the database with the hashed version of the provided password
                if (BCrypt.checkpw(userLoginRequest.getPassword(), user.getPassword())) {
                    // If login is successful, generate JWT token
                    String token = userService.generateJWTToken(user);
                    // Return the JWT token in the response
                    return ResponseEntity.ok().body(token);
                }
            }

            // If user does not exist or password does not match, return unauthorized status
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (UserValidationException e) {
            // If validation fails, return bad request status with error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userJPAService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchByUsername(@RequestParam String username) {
        // Search for users based on the provided username
        List<User> users = userService.searchByUsername(username);

        if (!users.isEmpty()) {
            return ResponseEntity.ok().body(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}