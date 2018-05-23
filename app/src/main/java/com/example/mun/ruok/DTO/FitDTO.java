package com.example.mun.ruok.DTO;

public class FitDTO {

    private int Fit_hour;
    private int Fit_minute;
    private int Fit_max_heart_rate;
    private int Fit_min_heart_rate;

    public FitDTO() {
    }

    public int getFitHour() {
        return Fit_hour;
    }
    public void setFitHour(int fit_hour) {
        Fit_hour = fit_hour;
    }

    public int getFitMinute() {
        return Fit_minute;
    }

    public void setFitMinute(int fit_minute) {
        Fit_minute = fit_minute;
    }

    public int getFitMaxHeartRate() {
        return Fit_max_heart_rate;
    }

    public void setFitMaxHeartRate(int fit_max_heart_rate) {
        Fit_max_heart_rate = fit_max_heart_rate;
    }

    public int getFitMinHeartRate() {
        return Fit_min_heart_rate;
    }

    public void setFitMinHeartRate(int fit_min_heart_rate) {
        Fit_min_heart_rate = fit_min_heart_rate;
    }

    public void setFitData(int fit_hour, int fit_minute, int fit_max_heart_rate, int fit_min_heart_rate) {
        Fit_hour = fit_hour;
        Fit_minute = fit_minute;
        Fit_max_heart_rate = fit_max_heart_rate;
        Fit_min_heart_rate = fit_min_heart_rate;
    }
}
