package com.ecommerce.backend.service.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.dto.UserDto;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.UserRole;
import com.ecommerce.backend.exception.InvalidCredentialsException;
import com.ecommerce.backend.exception.NotFoundException;
import com.ecommerce.backend.mapper.EntityDtoMapper;
import com.ecommerce.backend.repository.UserRepo;
import com.ecommerce.backend.security.JwtUtils;
import com.ecommerce.backend.service.interf.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EntityDtoMapper entityDtoMapper;
    private final JavaMailSender emailSender;

    @Override
    public Response registerUser(UserDto registrationRequest, HttpServletResponse response) {
        UserRole role = UserRole.USER;

        if (registrationRequest.getRole() != null && registrationRequest.getRole().equalsIgnoreCase("admin")) {
            role = UserRole.ADMIN;
        }

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .build();

        User savedUser = userRepo.save(user);
        System.out.println(savedUser);
        Cookie jwtCookie = new Cookie("dataRegister", String.format("email=%s", user.getEmail()));
        jwtCookie.setHttpOnly(true);  // Không cho JavaScript truy cập
        jwtCookie.setSecure(true);    // Chỉ gửi qua HTTPS
        jwtCookie.setPath("/");       // Phạm vi của cookie
        jwtCookie.setMaxAge(60 * 60 * 24);  // Thời gian sống của cookie: 1 ngày
        response.addCookie(jwtCookie);
        // Tạo token JWT
        String token = jwtUtils.generateToken(savedUser);

  
        // Gửi email với token
        sendConfirmationEmail(savedUser.getEmail(), token);

   
        return Response.builder()
                .status(200)
                .message("User Successfully Added")
                .build();
    }

    // Phương thức gửi email xác nhận
    private void sendConfirmationEmail(String toEmail, String token) {
        String subject = "Email Confirmation for Registration";
        String body = "Thank you for registering! Please use the following token to complete your registration process: " + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        emailSender.send(message);
        log.info("Confirmation email sent to: " + toEmail);
    }

    

    @Override
    public Response loginUser(LoginRequest loginRequest) {

        User user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new NotFoundException("Email not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Password does not match");
        }
        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true); // Chỉ dùng trên HTTPS
    refreshTokenCookie.setPath("/auth/refresh"); // Chỉ gửi cookie đến endpoint refresh
    refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
    response.addCookie(refreshTokenCookie);

        return Response.builder()
                .status(200)
                .message("User Successfully Logged In")
                .token(token)
                .expirationTime("6 Month")
                .role(user.getRole().name())
                .build();
    }

    @Override
    public Response getAllUsers() {

        List<User> users = userRepo.findAll();
        List<UserDto> userDtos = users.stream()
                .map(entityDtoMapper::mapUserToDtoBasic)
                .toList();

        return Response.builder()
                .status(200)
                .userList(userDtos)
                .build();
    }

    @Override
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String  email = authentication.getName();
        log.info("User Email is: " + email);
        return userRepo.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User Not found"));
    }

    @Override
    public Response getUserInfoAndOrderHistory() {
        User user = getLoginUser();
        UserDto userDto = entityDtoMapper.mapUserToDtoPlusAddressAndOrderHistory(user);

        return Response.builder()
                .status(200)
                .user(userDto)
                .build();
    }
}
