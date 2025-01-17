package com.ecommerce.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.dto.UserDto;
import com.ecommerce.backend.service.interf.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Response> registerUser(@ModelAttribute UserDto registrationRequest, HttpServletResponse response) {
        System.out.println(registrationRequest);
        return ResponseEntity.ok(userService.registerUser(registrationRequest, response));
    }


    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
public ResponseEntity<Response> loginUser(@ModelAttribute LoginRequest loginRequest) {
    return ResponseEntity.ok(userService.loginUser(loginRequest));
}

}