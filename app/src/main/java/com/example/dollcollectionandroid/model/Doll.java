package com.example.dollcollectionandroid.model;

public class Doll {
    // VARIABLES
    private int id;              // added for SQL stuff
    private String imagePath;
    private String name;
    private String brand;
    private String model;
    private int year;
    private String description;
    private String gender;       // hint becomes gender as hint is useless
    // extra variables
    private String birthDate;    // Format: "DD/MM/YYYY"
    private String birthTime;    // Format: "HH:mm" (24h)



    // CONSTRUCTOR: How we create a new Doll
    public Doll(int id, String imagePath, String name, String brand, String model, int year, String description,
                String gender, String birthDate, String birthTime) {
        this.id = id;
        this.imagePath = imagePath;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.description = description;
        // extra variables added
        this.gender = gender;
        this.birthDate = birthDate;
        this.birthTime = birthTime;
    }


    // GETTERS: Used by JavaFX to display the data
    public int getId() { return id; }
    public String getImagePath() { return imagePath; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getDescription() { return description; }
    public String getGender() { return gender; }
    public String getBirthDate() { return birthDate; }
    public String getBirthTime() { return birthTime; }


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
    public void setYear(int year) {
        this.year = year;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
}