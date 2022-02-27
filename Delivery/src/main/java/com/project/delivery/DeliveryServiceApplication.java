package com.project.delivery;

import com.project.delivery.service.DBInitializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliveryServiceApplication implements CommandLineRunner{

	@Autowired
	DBInitializer initializer;

	public static void main(String[] args) {
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(initializer.initAllTables());
		
	}

}
