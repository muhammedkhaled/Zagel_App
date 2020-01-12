package com.example.Zagel_App.models;

public class Contacts {

    private String device_token, image, name, status, uId;
    private userState userState;

    public Contacts() {
    }

    public Contacts(String device_token, String image, String name, String status, String uId, com.example.Zagel_App.models.userState userState) {
        this.device_token = device_token;
        this.image = image;
        this.name = name;
        this.status = status;
        this.uId = uId;
        this.userState = userState;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public com.example.Zagel_App.models.userState getUserState() {
        return userState;
    }

    public void setUserState(com.example.Zagel_App.models.userState userState) {
        this.userState = userState;
    }
}
