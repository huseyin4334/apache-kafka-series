package org.example.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PaymentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
