package com.choosemuse.example.libmuse;

import java.util.ArrayList;

public class User {
    private String[] name;
    private String email;
    private String deviceID;
    private ArrayList<String> museRuns;


    public User(String[] name, String email, String deviceID, ArrayList<String> museRuns) {
        this.name = name;
        this.email = email;
        this.deviceID = deviceID;
        this.museRuns = museRuns;
        }


    public User(String newDeviceID) {
        this.name = new String[]{"", ""};
        this.email = "";
        this.deviceID = newDeviceID;
        this.museRuns = new ArrayList<>();
    }

    /**
     * Logs current user information
     */
    public User() {
        this.name = new String[]{"", ""};
        this.email = "";
        this.deviceID = "";
        this.museRuns = new ArrayList<>();
    }

    //TODO: retrieve user information from document

    public String[] getName() {
        return name;
    }
    public void setName(String first, String last) {
        this.name = new String[]{first, last};
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public ArrayList<String> getMuseRuns() {
        return museRuns;
    }

    public void setMuseRuns(ArrayList<String> museRuns) {
        this.museRuns = museRuns;
    }

    public void addToMuseRun(String muse){
        this.museRuns.add(muse);
    }
}
