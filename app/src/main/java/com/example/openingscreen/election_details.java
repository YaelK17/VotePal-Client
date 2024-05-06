package com.example.openingscreen;

public class election_details {
    int image;
    String election_name;
    String due_date;

    public election_details(int image, String election_name, String due_date) {
        this.image = image;
        this.election_name = election_name;
        this.due_date = due_date;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getElection_name() {
        return election_name;
    }

    public void setElection_name(String election_name) {
        this.election_name = election_name;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }
}
