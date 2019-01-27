package com.iosm.chat;

public class Group_Messages {

    private String from, date,name , message, time;

    public Group_Messages(){

    }

    public Group_Messages(String date, String message, String name, String time, String from) {
        this.date = date;
        this.message = message;
        this.time = time;
        this.from = from;
        this.name = name;

    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
