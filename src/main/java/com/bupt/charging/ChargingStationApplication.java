package com.bupt.charging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargingStationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingStationApplication.class, args);
    }
}
