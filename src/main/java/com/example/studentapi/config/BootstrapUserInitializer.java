package com.example.studentapi.config;

import com.example.studentapi.user.AppUser;
import com.example.studentapi.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public BootstrapUserInitializer(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.username}") String username,
            @Value("${app.bootstrap.password}") String password) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap username and password must be configured");
        }

        if (!userRepository.existsByUsername(username)) {
            AppUser user = new AppUser(
                    username,
                    passwordEncoder.encode(password));

            userRepository.save(user);
        }
    }
}
