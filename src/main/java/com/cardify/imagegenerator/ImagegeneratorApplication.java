package com.cardify.imagegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// exclude 할 두 개의 자동설정 클래스 import
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;

@SpringBootApplication(
  exclude = {
    HttpClientAutoConfiguration.class,
    RestClientAutoConfiguration.class
  }
)
public class ImagegeneratorApplication {
  public static void main(String[] args) {
    SpringApplication.run(ImagegeneratorApplication.class, args);
  }
}