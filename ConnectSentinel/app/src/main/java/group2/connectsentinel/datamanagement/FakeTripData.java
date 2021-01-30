package group2.connectsentinel.datamanagement;

import java.util.HashMap;
import java.util.Map;

import group2.connectsentinel.data.Coordinates;
import group2.connectsentinel.data.Trip;

public class FakeTripData implements TripDataSource {

    private Map<Long, Trip> tripMap;
    private static final FakeTripData tripDB = new FakeTripData();

    private FakeTripData() {
        tripMap = new HashMap<>();
    }

    public static TripDataSource getInstance(){
        return tripDB;
    }

    @Override
    public boolean addTrip(long userID, Trip t) {
        tripMap.put(userID, t);
        return true;
    }

    @Override
    public boolean addLocationToTrip(long userID, Coordinates coord) {
        Trip t = tripMap.get(userID);
        if(t == null) {
            return false;
        }
        t.addLocation(coord);
        return true;
    }

    @Override
    public boolean endTrip(long userID) {
        Trip t = tripMap.get(userID);
        if(t == null) {
            return false;
        }
        t.endTrip();
        return true;
    }
}
