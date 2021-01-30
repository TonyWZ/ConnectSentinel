package group2.connectsentinel.datamanagement;

import group2.connectsentinel.data.Coordinates;

public interface LocationDataSource {

    boolean updateLocation(long id, Coordinates coord);

}
