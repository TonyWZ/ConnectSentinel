package group2.connectsentinel.datamanagement;

import group2.connectsentinel.data.UserProfile;
import java.util.Map;

public interface UserDataSource {

    //Return the user profile for a specific user id
    //Used for displaying the profile of the current user
    //RETURN NULL IF ID DOES NOT EXIST
    UserProfile requestUserProfile(long id);

    //Add a given user profile to the data source
    //For registration of new user
    boolean addUserProfile(UserProfile profile, String password);

    //Given an id and the hash of a password
    //Check if the combination is correct
    boolean checkPassword(long id, String passwordHash);

    //Edit existing profile for setting
    boolean editUserProfile(UserProfile profile, String password);

    //Return password
    String requestPassword(long id);
}