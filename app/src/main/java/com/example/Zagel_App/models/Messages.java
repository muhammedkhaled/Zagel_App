package com.example.Zagel_App.models;

public class Messages {

    private String from, message, type, messageID, time, date, name, to;

    public Messages(String from, String message, String type, String messageID, String time, String date, String name, String to) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
        this.to = to;
    }

    public Messages() {
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
