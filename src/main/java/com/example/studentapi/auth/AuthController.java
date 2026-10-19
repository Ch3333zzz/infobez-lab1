package com.example.studentapi.auth;

import com.example.studentapi.security.JwtService;
import com.example.studentapi.user.AppUser;
import com.example.studentapi.user.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        AppUser user = userRepository
                .findByUsername(request.username().trim())
                .orElse(null);

        if (user == null
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error",
                            "Invalid username or password"));
        }

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        "Bearer",
                        jwtService.getExpirationSeconds()));
    }
}
