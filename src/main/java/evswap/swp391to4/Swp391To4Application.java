package evswap.swp391to4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "evswap.swp391to4")
public class Swp391To4Application {

    public static void main(String[] args) {
        SpringApplication.run(Swp391To4Application.class, args);
    }

}
