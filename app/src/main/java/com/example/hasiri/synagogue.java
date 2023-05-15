package com.example.hasiri;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class synagogue {
    List<PublicPrayer> publicPrayerList;
    LatLng latLng;
    String Name;

    public synagogue(List<PublicPrayer> publicPrayerList , LatLng latLng , String Name) {
        this.publicPrayerList = publicPrayerList;
        this.latLng = latLng;
        this.Name = Name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public List<PublicPrayer> getPublicPrayerList() {
        return publicPrayerList;
    }

    public void setPublicPrayerList(List<PublicPrayer> publicPrayerList) {
        this.publicPrayerList = publicPrayerList;
    }
}
