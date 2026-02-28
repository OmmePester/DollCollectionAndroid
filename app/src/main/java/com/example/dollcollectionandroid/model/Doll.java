package com.example.dollcollectionandroid.model;

public class Doll {
    // VARIABLES
    private int id;    // added for sql stuff
    private String imagePath;
    private String name;
    private String hint;
    private String description;
    private String brand;
    private String model;
    private int year;
    // Extra variables that will be added by user

    // CONSTRUCTOR: How we create a new Doll
    public Doll(int id, String imagePath, String name, String hint, String description, String brand, String model, int year) {
        this.id = id;
        this.imagePath = imagePath;
        this.name = name;
        this.hint = hint;
        this.description = description;
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    // GETTERS: Used by JavaFX to display the data
    public int getId() { return id; }
    public String getImagePath() { return imagePath; }
    public String getName() { return name; }
    public String getHint() { return hint; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }

    // DELETER: Clears the description
    public void deleteDescription() {
        this.description = "";
    }//MAY DELETE LATER AS NO USE

    // SETTER: Allows the user to edit the description
    public void setName(String name) {
        this.name = name;
    }
    public void setHint(String hint) {
        this.hint = hint;
    }
    public void setDescription(String description) {
        this.description = description;
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


}
