package com.websvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.websvc.services.MediaInfoService;
import com.websvc.services.StorageProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    @Bean
    @Profile("!test")
    CommandLineRunner init(MediaInfoService mediaInfoService) {
        return (args) -> {
        		//loads all the mp3 files stored on disk into our database
        		mediaInfoService.loadMediaFilesFromDisk();
        };
    }

}