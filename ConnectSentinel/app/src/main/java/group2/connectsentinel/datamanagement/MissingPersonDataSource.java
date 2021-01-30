package group2.connectsentinel.datamanagement;

import java.util.List;

import group2.connectsentinel.data.MissingPerson;

public interface MissingPersonDataSource {

    // get the Last reported n people.
    // If there are fewer than n people, then return all the missing people left.
    List<MissingPerson> getFirst(int n);

    // add missing person to the database.
    void add(MissingPerson person);

    // delete the given person from the List.
    boolean deletePerson(MissingPerson person);

}
