package com.techforge;

import com.techforge.entity.User;
import com.techforge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class TechForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechForgeApplication.class, args);
    }

    @Bean
    public CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User user = new User();
                user.setUsername("xiaohui");
                user.setPassword(passwordEncoder.encode("666666"));
                userRepository.save(user);
                System.out.println(">>> 默认用户已创建: xiaohui / 666666");
            }
        };
    }
}