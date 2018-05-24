package com.example.mun.ruok.DTO;

public class GuardianDTO {
    private String userEmailID; // email 주소에서 @ 이전까지의 값.
    private String fcmToken;
    private boolean userType;

    public GuardianDTO() {
    }

    public String getUserEmailID() { return userEmailID; }
    public void setUserEmailID(String UserEmailID) { this.userEmailID = UserEmailID; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String FcmToken) { this.fcmToken = FcmToken; }

    public boolean getUserType() { return userType; }
    public void setUserType(boolean userType) { this.userType = userType; }

    public void setGuardianData(String userEmailID, String fcmToken, boolean userType) {
        this.userEmailID = userEmailID;
        this.fcmToken = fcmToken;
        this.userType = userType;
    }
}
