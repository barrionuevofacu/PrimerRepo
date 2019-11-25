package com.example.trabajofinalobjetos15_7_2019.DTOs;

import com.google.gson.annotations.SerializedName;

public class ImageDTO {

    @SerializedName("id")
    private int id ;
    @SerializedName("picture")
    private String Picture;
    @SerializedName("raza1")
    private String raza1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getRaza1() { return raza1; }

    public void setRaza1(String raza1) { this.raza1 = raza1; }

}
