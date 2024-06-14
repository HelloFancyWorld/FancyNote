package com.example.myapplication.network;

public class UpdatePWRequest {
    private String oldpw;
    private String newpw;

    public UpdatePWRequest(String oldpw, String newpw) {
        this.oldpw = oldpw;
        this.newpw = newpw;
    }

    public String getNewpw() {
        return newpw;
    }

    public String getOldpw() {
        return oldpw;
    }

    public void setNewpw(String newpw) {
        this.newpw = newpw;
    }

    public void setOldpw(String oldpw) {
        this.oldpw = oldpw;
    }
}
