package com.example.ibsbms;

import com.example.ibsbms.entity.Shareholder;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevBalanceSeeder {

    @Bean
    public CommandLineRunner seedTestBalance(ShareholderRepository shareholderRepository) {
        return args -> {

            String folioToSeed = "1478966559";
            long balanceToSet = 5000L;

            shareholderRepository.findByFolioBoAndIsValid(folioToSeed, 1)
                    .ifPresent(shareholder -> {
                        shareholder.setBalance(balanceToSet);
                        shareholderRepository.save(shareholder);
                        System.out.println("Seeded balance " + balanceToSet + " for Folio " + folioToSeed);
                    });
        };
    }
}