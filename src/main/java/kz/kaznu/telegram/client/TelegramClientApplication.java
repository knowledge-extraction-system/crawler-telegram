package kz.kaznu.telegram.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class TelegramClientApplication {

  public static void main(String[] args) {
    System.out.println("JavaLibraryPath: " + System.getProperty("java.library.path"));
    SpringApplication.run(TelegramClientApplication.class, args);
  }

}