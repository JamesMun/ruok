package com.example.mun.ruok.DTO;

public class GuardianDTO {
    public String userEmailID; // email 주소에서 @ 이전까지의 값.
    public String fcmToken;
    public int userType;

    public GuardianDTO() {
    }

    public String getUserEmailID() { return userEmailID; }
    public void setUserEmailID(String UserEmailID) { this.userEmailID = UserEmailID; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String FcmToken) { this.fcmToken = FcmToken; }

    public int getUserType() { return userType; }
    public void setUserType(int userType) { this.userType = userType; }

    public void setGuardianData(String userEmailID, String fcmToken, int userType) {
        this.userEmailID = userEmailID;
        this.fcmToken = fcmToken;
        this.userType = userType;
    }
}
