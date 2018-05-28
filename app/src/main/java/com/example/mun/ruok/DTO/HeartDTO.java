package com.example.mun.ruok.DTO;

public class HeartDTO {
    private int HR;
    private String TS;
    private Double LAT;
    private Double LON;

    public void setHeartRate(int HR) {
        this.HR = HR;
    }

    public int getHeartRate() {
        return HR;
    }

    public void setTimeStamp(String TS) {
        this.TS = TS;
    }

    public String getTimeStamp() {
        return TS;
    }

    public void setLatitude(Double LAT) {
        this.LAT = LAT;
    }

    public Double getLatitude() {
        return LAT;
    }

    public void setLongitude(Double LON) {
        this.LON = LON;
    }

    public Double getLongitude() {
        return LON;
    }

    public void setLocation(Double LAT, Double LON) {
        this.LAT = LAT;
        this.LON = LON;
    }

    public void setHeartData(int HR, String TS, Double LAT, Double LON) {
        this.HR = HR;
        this.TS = TS;
        this.LAT = LAT;
        this.LON = LON;
    }

    public boolean hasLocation() {
        if(this.LAT != null && this.LON != null) {
            return true;
        } else {
            return false;
        }
    }
}
