package com.example.mun.ruok.DTO;

public class ConnectDTO {
    private String ConnectionWith;
    private int CONNECTING_CODE;

    public ConnectDTO() {
    }

    public void setConnectingCode(int CONNECTING_CODE) {
        this.CONNECTING_CODE = CONNECTING_CODE;
    }

    public int getConnectingCode() {
        return CONNECTING_CODE;
    }

    public void setConnectionWith(String connectionWith) {
        ConnectionWith = connectionWith;
    }

    public String getConnectionWith() {
        return ConnectionWith;
    }

    public void setConnection(String ConnectionWith, int CONNECTING_CODE) {
        this.ConnectionWith = ConnectionWith;
        this.CONNECTING_CODE = CONNECTING_CODE;
    }
}
