package evswap.swp391to4;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Swp391To4Application {

    public static void main(String[] args) {
        SpringApplication.run(Swp391To4Application.class, args);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            // Mật khẩu gốc bạn muốn mã hóa
            String rawPassword = "123456";

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(rawPassword);

            System.out.println("====================================================================");
            System.out.println("Mật khẩu gốc: " + rawPassword);
            System.out.println("Mật khẩu đã mã hóa (BCrypt): " + encodedPassword);
            System.out.println("Hãy sao chép chuỗi mã hóa ở trên và cập nhật vào database.");
            System.out.println("====================================================================");
        };
    }
}
