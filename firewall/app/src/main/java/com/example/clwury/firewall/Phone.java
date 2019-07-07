package com.example.clwury.firewall;

public class Phone {
    private String number;
    private String time;
    public Phone(String number,String time){
        this.number=number;
        this.time=time;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
