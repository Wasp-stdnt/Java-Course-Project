package io.github.wasp_stdnt.passwordmanagerv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PasswordManagerV2Application {

    public static void main(String[] args) {
        SpringApplication.run(PasswordManagerV2Application.class, args);
    }

}
