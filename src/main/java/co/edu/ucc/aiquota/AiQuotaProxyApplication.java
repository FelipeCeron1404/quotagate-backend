package co.edu.ucc.aiquota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiQuotaProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiQuotaProxyApplication.class, args);
    }
}
