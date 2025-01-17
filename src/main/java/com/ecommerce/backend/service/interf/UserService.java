package com.ecommerce.backend.service.interf;

import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.dto.UserDto;
import com.ecommerce.backend.entity.User;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    Response registerUser(UserDto registrationRequest, HttpServletResponse response);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getLoginUser();
    Response getUserInfoAndOrderHistory();

}
