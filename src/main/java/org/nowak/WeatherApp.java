package org.nowak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Backend aplikacji progrnozy pogody stworzonej w ramach zadania rekrutacyjnego dla Codibly S.A.
 */
@SpringBootApplication
public class WeatherApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(WeatherApp.class, args);
    }
}
