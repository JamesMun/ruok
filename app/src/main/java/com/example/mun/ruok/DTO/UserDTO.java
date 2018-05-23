package com.example.mun.ruok.DTO;

public class UserDTO {
    private String userEmailID; // email 주소에서 @ 이전까지의 값.
    private String fcmToken;
    private int max_heart_rate;
    private int min_heart_rate;
    private int userType;

    public UserDTO() {
    }

    public String getUserEmailID() { return userEmailID; }
    public void setUserEmailID(String UserEmailID) { this.userEmailID = UserEmailID; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String FcmToken) { this.fcmToken = FcmToken; }

    public int getMaxHeartRate() { return max_heart_rate; }
    public void setMaxHeartRate(int max_heart_rate) { this.max_heart_rate = max_heart_rate; }

    public int getMinHeartRate() { return min_heart_rate; }
    public void setMinHeartRate(int min_heart_rate) { this.min_heart_rate = min_heart_rate; }

    public int getUserType() { return userType; }
    public void setUserType(int userType) { this.userType = userType; }

    public void setHeartRate(int max_heart_rate, int min_heart_rate) {
        this.max_heart_rate = max_heart_rate;
        this.min_heart_rate = min_heart_rate;
    }

    public void setUserData(String userEmailID, String fcmToken, int max_heart_rate, int min_heart_rate, int userType) {
        this.userEmailID = userEmailID;
        this.fcmToken = fcmToken;
        this.max_heart_rate = max_heart_rate;
        this.min_heart_rate = min_heart_rate;
        this.userType = userType;
    }
}
