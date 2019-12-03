package com.example.trabajofinalobjetos15_7_2019.DTOs;

import com.google.gson.annotations.SerializedName;

public class LocationDTO {

    @SerializedName("id")
    private int id;
    @SerializedName("userId")
    private int userId;
    @SerializedName("tag")
    private String tag;
    @SerializedName("color")
    private int color;
    @SerializedName("type")
    private short type;
    @SerializedName("coordinates")
    private String coordinates ;
    @SerializedName("imageId")
    private int imageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }



    public LocationDTO(String coordinates) {
        this.coordinates = coordinates;
    }

    public LocationDTO() {
        this.coordinates = "";
    }

    public String getData() {
        String data = "id: " + this.id + '\n';
        data += "userId: " + this.userId + '\n';
        data += "tag: " + this.tag + '\n';
        data += "color: " + this.color + '\n';
        data += "type: " + this.type + '\n';
        data += "coordinates: ";
        for (String location : this.coordinates.split(";")) {
            data += location + '\n';
        }
        return data;
    }


}
