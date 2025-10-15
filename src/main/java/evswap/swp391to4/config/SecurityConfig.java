package evswap.swp391to4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (chỉ dùng cho API, nếu có frontend cần bật lại)
                .csrf(csrf -> csrf.disable())
                // Bật CORS cho API với custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Cho phép tất cả request mà không cần login
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/legacy/**", "/", "/index.html", "/assets/**", "/build/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // Bean mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

