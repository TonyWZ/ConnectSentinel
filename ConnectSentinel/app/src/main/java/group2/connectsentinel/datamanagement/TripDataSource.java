package group2.connectsentinel.datamanagement;

import group2.connectsentinel.data.Coordinates;
import group2.connectsentinel.data.Trip;

public interface TripDataSource {

    public boolean addTrip(long userID, Trip t);

    public boolean addLocationToTrip(long userID, Coordinates coord);

    public boolean endTrip(long userID);

}