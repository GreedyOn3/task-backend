package org.nowak.models;

public class WeekSummary {
    private final double avgPressure;
    private final double avgSunshineHours;
    private final double maxTemp;
    private final double minTemp;
    private final String summary;

    public WeekSummary(double avgPressure, double avgSunshineHours, double maxTemp, double minTemp, String summary) {
        this.avgPressure = avgPressure;
        this.avgSunshineHours = avgSunshineHours;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.summary = summary;
    }

    public double getAvgPressure() {
        return avgPressure;
    }

    public double getAvgSunshineHours() {
        return avgSunshineHours;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public String getSummary() {
        return summary;
    }
}
