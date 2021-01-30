package group2.connectsentinel.datamanagement;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import group2.connectsentinel.data.UserProfile;

// VERY IMPORTANT!
//To use: First call FakeUserData.getInstance();
//to get an instance of UserDataSource
//Then call methods on that instance
public class FakeUserData {
/*
    private Map<Long, UserProfile> fakeProfileMap;
    private Map<Long, String> fakePasswordMap;
    private static final FakeUserData fakeDB = new FakeUserData();

    private FakeUserData() {
        Log.v("Constructor", "Creating a new instance of userData");
        fakeProfileMap = new HashMap<Long, UserProfile>();
        fakePasswordMap = new HashMap<>();
        UserProfile sylviePf = new UserProfile(1234560001, "Sylvie",1234560002, true, false);
        UserProfile tonyPf = new UserProfile(1234560002, "Tony",1234560003, false, true);
        UserProfile effiePf = new UserProfile(1234560003, "Effie",1234560004, true, true);
        UserProfile chelsiePf = new UserProfile(1234560004, "Chelsie", 1234560001, false, false);
        fakeProfileMap.put(1234560001l, sylviePf);
        fakeProfileMap.put(1234560002l, tonyPf);
        fakeProfileMap.put(1234560003l, effiePf);
        fakeProfileMap.put(1234560004l, chelsiePf);
        fakePasswordMap.put(1234560001l, "sylviepw");
        fakePasswordMap.put(1234560002l, "tonypw");
        fakePasswordMap.put(1234560003l, "effiepw");
        fakePasswordMap.put(1234560004l, "chelsiepw");
    }

    public static UserDataSource getInstance(){
        return fakeDB;
    }

    //Return the user profile for a specific user id
    //Used for displaying the profile of the current user
    public UserProfile requestUserProfile(long id) {
        return fakeProfileMap.get(id);
    }

    //Add a given user profile to the data source
    //For registration of new user
    public boolean addUserProfile(UserProfile profile, String password){
        fakeProfileMap.put(profile.getId(), profile);
        fakePasswordMap.put(profile.getId(), password);
        return true;
    }

    public boolean editUserProfile(UserProfile profile, String password) {
        fakeProfileMap.replace(profile.getId(), profile);
        fakePasswordMap.replace(profile.getId(), password);
        return true;
    }

    //Given an id and the hash of a password
    //Check if the combination is correct
    public boolean checkPassword(long id, String passwordHash){
        Log.v("Mine",passwordHash);
        String correctPW = fakePasswordMap.get(id);
        if(correctPW == null) {
            return false;
        } else {
            return correctPW.equals(passwordHash);
        }
    }

    public String requestPassword(long id) {
        return fakePasswordMap.get(id);
    }
    */
}
