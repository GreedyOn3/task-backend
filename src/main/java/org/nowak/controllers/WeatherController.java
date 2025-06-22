package org.nowak.controllers;

import org.nowak.models.Forecast;
import org.nowak.models.WeekSummary;
import org.nowak.services.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    //Endpoint 1 - zwraca prognozę pogody na 7 dni
    @GetMapping("/forecast")
    public ArrayList<Forecast> sevenDayForecast(@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
        validateClientInput(latitude, longitude);
        return weatherService.getSevenDayForecast(latitude, longitude);
    }

    //Endpoint 2 - zwraca podsumowanie najbliższego tygodnia
    @GetMapping("/summary")
    public WeekSummary weekSummary(@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
        validateClientInput(latitude, longitude);
        return weatherService.getWeekSummary(latitude, longitude);
    }
    

    private void validateClientInput(double lat, double lon) {
        if (lat < -90 || lat > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "[BŁĄD] Parametr 'latitude' powinien być w zakresie [-90;90]");
        }
        if (lon < -180 || lon > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "[BŁĄD] Parametr 'longitude' powinien być w zakresie [-180;180]");
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        return "[BŁĄD] Parametr '" + param + "' ma nieprawidłowy typ. Wymagana liczba zmiennoprzecinkowa (np. 12.345)";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingParams(MissingServletRequestParameterException ex) {
        return "[BŁĄD] Brakuje parametru: " + ex.getParameterName();
    }
}
