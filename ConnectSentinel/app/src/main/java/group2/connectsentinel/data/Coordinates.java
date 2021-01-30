package group2.connectsentinel.data;

public class Coordinates {

    private double latitude;
    private double longitude;

    public Coordinates(double la, double lo) {
        latitude = la;
        longitude = lo;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

}
