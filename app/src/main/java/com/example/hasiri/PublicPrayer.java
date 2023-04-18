package com.example.hasiri;

import java.io.Serializable;

public class PublicPrayer implements Serializable {
    public int signUps;
    int moed,hour,minute;
    //int Time;
    double lat;
    double lag;
    String Heara;
    static int i = 0;

    public PublicPrayer() {

    }


    public PublicPrayer(int moed, double lat, double lag,int hour,int minute,String Heara) {
        this.signUps = 0;
        this.moed = moed;
        this.lat = lat;
        this.lag = lag;
        this.hour = hour;
        this.minute = minute;
        this.Heara = Heara;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLag() {
        return lag;
    }

    public void setLag(double lag) {
        this.lag = lag;
    }


    public int getSignUps() {
        return signUps;
    }

    public void setSignUps(int signUps) {
        this.signUps = signUps;
    }

    public int getMoed() {
        return moed;
    }

    public void setMoed(int moed) {
        this.moed = moed;
    }

   public int getHour() {return this.hour;}

    public void setHour(int hour) {this.hour = hour;}

    public int getMinute() {return minute;}

    public void setMinute(int minute) {this.minute = minute;}

    public String getHeara() {return Heara; }

}
