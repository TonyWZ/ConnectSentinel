package group2.connectsentinel.data;

import java.util.Date;

public class Report {

    private long fromUser;
    private long toUser;
    private String message;
    private Date time;
    private String location;

    public Report(long fromUser, long toUser, String mes, String location) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = mes;
        this.time = new Date();
        this.location = location;
    }

}
