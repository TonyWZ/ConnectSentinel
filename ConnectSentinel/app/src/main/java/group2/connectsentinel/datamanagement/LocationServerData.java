package group2.connectsentinel.datamanagement;

import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.UpdateLocationTask;
import group2.connectsentinel.data.Coordinates;

public class LocationServerData implements LocationDataSource {

    private static final LocationServerData locationdb = new LocationServerData();

    public static LocationDataSource getInstance(){
        return locationdb;
    }

    public boolean updateLocation(long id, Coordinates coord) {
        String[] keys = new String[] {"latitude", "longitude", "id"};
        String[] values = new String[] {Double.toString(coord.getLatitude()), Double.toString(coord.getLongitude()), Long.toString(id)};
        String data = PostDataEncoder.encodePostData(keys, values);
        new UpdateLocationTask().execute(data);
        return true;
    }

}
