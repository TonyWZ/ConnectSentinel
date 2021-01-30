package group2.connectsentinel.data;

public class BanUser {
    private long id;
    private String password;

    public BanUser(long id, String password) {
        this.id = id;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
