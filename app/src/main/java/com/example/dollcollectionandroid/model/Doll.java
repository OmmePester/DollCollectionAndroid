package com.example.dollcollectionandroid.model;

public class Doll {
    // VARIABLES
    private int id;    // added for sql stuff
    private String imagePath;
    private String name;
    private String brand;
    private String model;
    private String description;
    private int year;    // will turn into date later
    private String hint;    // hint becomes gender as hint is useless
    // Extra variables for Natal Chart / Big Three
    private String birthDate;    // Format: "DD/MM/YYYY"
    private String birthTime;    // Format: "HH:mm" (24h)
    private String birthCity;
    private double latitude;
    private double longitude;


    // CONSTRUCTOR: How we create a new Doll
    public Doll(int id, String imagePath, String name, String brand, String model, String description, int year, String hint,
                String birthDate, String birthTime, String birthCity, double latitude, double longitude) {
        this.id = id;
        this.imagePath = imagePath;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.description = description;
        this.year = year;
        this.hint = hint;
        // extra variables added
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.birthCity = birthCity;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    // GETTERS: Used by JavaFX to display the data
    public int getId() { return id; }
    public String getImagePath() { return imagePath; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getDescription() { return description; }
    public int getYear() { return year; }
    public String getHint() { return hint; }
    public String getBirthDate() { return birthDate; }
    public String getBirthTime() { return birthTime; }
    public String getBirthCity() { return birthCity; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }


    // SETTER: Allows the user to edit the description
    public void setName(String name) {
        this.name = name;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public void setHint(String hint) {
        this.hint = hint;
    }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
    public void setBirthCity(String birthCity) { this.birthCity = birthCity; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}