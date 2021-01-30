package group2.connectsentinel.datamanagement;

import java.util.HashMap;
import java.util.Map;

import group2.connectsentinel.data.Coordinates;

public class FakeLocationData implements LocationDataSource {

    private Map<Long, Coordinates> locationMap;

    private static final FakeLocationData locationDB = new FakeLocationData();

    private FakeLocationData(){
        locationMap = new HashMap<>();
    }

    public static FakeLocationData getInstance(){
        return locationDB;
    }

    @Override
    public boolean updateLocation(long id, Coordinates coord) {
        return false;
    }
}
