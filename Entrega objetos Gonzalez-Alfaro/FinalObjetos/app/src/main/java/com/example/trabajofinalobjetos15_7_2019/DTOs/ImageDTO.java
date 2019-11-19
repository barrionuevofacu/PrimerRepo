package com.example.trabajofinalobjetos15_7_2019.DTOs;

import com.google.gson.annotations.SerializedName;

public class ImageDTO {

    @SerializedName("id")
    private int id ;
    @SerializedName("picture")
    private String Picture;

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



}
