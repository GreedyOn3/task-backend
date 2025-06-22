package org.nowak.models;

public class Forecast {
    private final String date;
    private final int weatherCode;
    private final double maxTemp;
    private final double minTemp;
    private final double generatedEnergy;

    public Forecast(String date, int weatherCode, double maxTemp, double minTemp, double generatedEnergy) {
        this.date = date;
        this.weatherCode = weatherCode;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.generatedEnergy = generatedEnergy;
    }

    public String getDate() {
        return date;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getGeneratedEnergy() {
        return generatedEnergy;
    }
}
