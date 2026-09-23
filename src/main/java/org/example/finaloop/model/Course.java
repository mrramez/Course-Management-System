package org.example.finaloop.model;

public class Course {

    private int id;
    private String name;
    private String description;
    private double price;
    private int trainerId;
    private String imagePath;
    private Trainer trainer;
    private int studentsCount;

    public Course() {
    }

    public Course(int id, String name, String description, double price, int trainerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.trainerId = trainerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public String getTrainerName() {
        if (trainer == null) {
            return null;
        }
        return trainer.getUserName();
    }

    public int getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(int studentsCount) {
        this.studentsCount = studentsCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
