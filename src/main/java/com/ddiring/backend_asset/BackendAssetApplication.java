package com.ddiring.backend_asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BackendAssetApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendAssetApplication.class, args);
	}

}
