package com.example.hasiri;

import com.google.android.gms.maps.model.LatLng;

public class Minyan_model {

    String SignsUp , Time, Address ,Distance;
    int image;
    LatLng latLng;
    String id;



    public Minyan_model(String signsUp, String time, String address, int image , LatLng latLng, String Distance, String id ) {
        SignsUp = signsUp;
        Time = time;
        Address = address;
        this.image = image;
        this.latLng = latLng;
        this.Distance = Distance;
        this.id = id;
    }

    public String getSignsUp() {
        return SignsUp;
    }

    public String getTime() {
        return Time;
    }

    public String getAddress() {
        return Address;
    }


    public LatLng getLatLng() {
        return latLng;
    }

    public int getImage() {
        return image;
    }

    public String getDistance() {
        return Distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSignsUp(String signsUp) {
        SignsUp = signsUp;
    }

    public void setTime(String time) {
        Time = time;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setLatLng(LatLng latLng) {this.latLng = latLng;}

    public void setDistance(String distance) {
        Distance = distance;
    }
}
