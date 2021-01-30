package group2.connectsentinel.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Trip {

    private String destination;
    private List<Coordinates> coordList;
    private long emergencyContact;
    private Date startTime;
    private Date endTime;

    public Trip(String dest, long eCtct, Date start) {
        destination = dest;
        emergencyContact = eCtct;
        startTime = start;
        coordList = new LinkedList<>();
    }

    public void addLocation(Coordinates loc){
        coordList.add(loc);
    }

    public void endTrip(){
        endTime = new Date();
    }

}
