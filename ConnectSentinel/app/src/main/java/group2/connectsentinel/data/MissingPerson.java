package group2.connectsentinel.data;

public class MissingPerson {

    private String lastSeenStreet;
    private String lastSeenCity;
    private String lastSeenState;
    private String description;
    private String name;
    private long contact;
    private String image;
    private boolean hasImage;

    public MissingPerson(String lastSeenStreet, String lastSeenCity, String lastSeenState,
                         String description, String name, long contact, boolean hasImage) {
        this.lastSeenStreet = lastSeenStreet;
        this.lastSeenCity = lastSeenCity;
        this.lastSeenState = lastSeenState;
        this.description = description;
        this.name = name;
        this.contact = contact;
        this.hasImage = hasImage;
    }

    public MissingPerson(String lastSeenStreet, String lastSeenCity, String lastSeenState,
                         String description, String name, long contact, String image,
                         boolean hasImage) {
        this.lastSeenStreet = lastSeenStreet;
        this.lastSeenCity = lastSeenCity;
        this.lastSeenState = lastSeenState;
        this.description = description;
        this.name = name;
        this.contact = contact;
        this.image = image;
        this.hasImage = hasImage;
    }

    public String getDescription() {
        return description;
    }

    public String getLastSeenStreet() {
        return lastSeenStreet;
    }

    public String getLastSeenCity() {
        return lastSeenCity;
    }

    public String getLastSeenState() {
        return lastSeenState;
    }

    public String getName() {
        return name;
    }

    public long getContact() {
        return contact;
    }

    public String getImage() {
        return image;
    }

    public boolean getHasImage() {
        return hasImage;
    }
}
