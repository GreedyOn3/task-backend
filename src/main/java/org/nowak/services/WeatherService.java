package org.nowak.services;

import org.nowak.models.Forecast;
import org.nowak.models.WeekSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SuppressWarnings("unchecked")
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();


    public ArrayList<Forecast> getSevenDayForecast(double latitude, double longitude) {
        String url = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,weathercode,sunshine_duration")
                .queryParam("timezone", "auto")
                .build()
                .toUriString();

        //Wartości potrzebne do wyliczenia szacowanej wygenerowanej energi przy użyciu instalacji fotowoltanicznej
        double installationPower = 2.5; //Moc instalacji[Kw]
        double panelEfficiency = 0.2; //Efektywność paneli

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("daily")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Błędna odpowiedź API w getSevenDayForecast");
            }

            // Pobieranie danych dziennych
            Map<String, List<Object>> dailyForecast = (Map<String, List<Object>>) response.get("daily");
            if (dailyForecast == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] 'daily' w getSevenDayForecast jest puste");
            }
            
            List<Object> dates = dailyForecast.get("time");
            List<Object> maxTemps = dailyForecast.get("temperature_2m_max");
            List<Object> minTemps = dailyForecast.get("temperature_2m_min");
            List<Object> weatherCodes = dailyForecast.get("weathercode");
            List<Object> sunshineDuration = dailyForecast.get("sunshine_duration");

            if (dates == null || maxTemps == null || minTemps == null || weatherCodes == null || sunshineDuration == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Odpowiedź API w getSevenDayForecast nie zawiera wszystkich wymaganych pól");
            }

            ArrayList<Forecast> forecastList = new ArrayList<>();

            for (int i = 0; i < dates.size(); i++) {


                double generatedEnergy =  installationPower * (((double) sunshineDuration.get(i))/3600) * panelEfficiency;

                forecastList.add(new Forecast(
                        (String) dates.get(i),
                        ((int) weatherCodes.get(i)),
                        ((double) maxTemps.get(i)),
                        ((double) minTemps.get(i)),
                        (double) Math.round(generatedEnergy * 10) /10
                ));
            }

            return forecastList;

        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Nie udało się połączyć z API przy prognozie pogody:", e);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "[BŁĄD] Nieoczekiwany błąd podczas przetwarzania prognozy", e);
        }
    }

    public WeekSummary getWeekSummary(double latitude, double longitude) {
        String url = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "weathercode,temperature_2m_max,temperature_2m_min,sunshine_duration")
                .queryParam("hourly", "pressure_msl")
                .queryParam("timezone", "auto")
                .build()
                .toUriString();
        //Kody pogodowe odpowiadające pogodzie z opadami atmosferycznymi
        Set<Integer> precipitationWeatherCodes = Set.of(51,53,55,80,81,82,61,63,65,56,57,66,67,77,85,86,71,73,75,96,99);
        //Ilość dni z opadami
        int precipitationDaysCount = 0;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("daily")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Błędna odpowiedź API w getWeekSummary");
            }

            // Pobieranie danych dziennych
            Map<String, List<Object>> dailyForecast = (Map<String, List<Object>>) response.get("daily");
            if (dailyForecast == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] 'daily' w getWeekSummary jest puste");
            }

            // Pobieranie danych godzinowych(dla ciśnienia)
            Map<String, List<Object>> hourlyForecast = (Map<String, List<Object>>) response.get("hourly");
            if (hourlyForecast == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] 'hourly' w getWeekSummary jest puste");
            }

            List<Object> weatherCodes = dailyForecast.get("weathercode");
            List<Object> maxTemps = dailyForecast.get("temperature_2m_max");
            List<Object> minTemps = dailyForecast.get("temperature_2m_min");
            List<Object> sunshineDuration = dailyForecast.get("sunshine_duration");
            List<Object> pressure = hourlyForecast.get("pressure_msl");
            if (weatherCodes == null || maxTemps == null || minTemps == null || sunshineDuration == null || pressure == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Odpowiedź API w getWeekSummary nie zawiera wszystkich wymaganych pól");
            }

            double sumPressure = 0;
            double sumSunshineHours = 0;
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;

            // Liczenie średnich z danych dziennych
            for (int i = 0; i < weatherCodes.size(); i++) {
                int weatherCode = (int) weatherCodes.get(i);
                double max = (double) maxTemps.get(i);
                double min = (double) minTemps.get(i);
                double sunSec = (double) sunshineDuration.get(i);

                if (max > maxTemp) maxTemp = max;
                if (min < minTemp) minTemp = min;
                sumSunshineHours += sunSec / 3600;

                //Sprawdzamy czy dzień jest dniem z opadami
                if (precipitationWeatherCodes.contains(weatherCode)) {
                    precipitationDaysCount++;
                }
            }

            // Liczenie sumy ciśnienia przez cały tydzień w celu wliczenia średniej
            for (Object val : pressure) {
                sumPressure += ((double) val);
            }

            double avgPressure = (double) Math.round((sumPressure / pressure.size()) * 100) /100;
            double avgSunshineHours = (double) Math.round((sumSunshineHours / sunshineDuration.size() * 100)) /100;

            String summary = precipitationDaysCount > 3 ? "Tydzień w większości z opadami" : "Tydzień w większości bez opadów";

            return new WeekSummary(avgPressure, avgSunshineHours, maxTemp, minTemp, summary);

        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "[BŁĄD] Nie udało się połączyć z API przy podsumowaniu tygodnia: ", e);
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "[BŁĄD] Nieoczekiwany błąd podczas przetwarzania podsumowania tygodnia", e);
        }
    }
}
