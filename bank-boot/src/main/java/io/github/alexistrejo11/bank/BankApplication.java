package io.github.alexistrejo11.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {
		"io.github.alexistrejo11.bank",
		"io.github.alexisTrejo11.bank", // Fails if remove
})
public class BankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankApplication.class, args);
	}

}
