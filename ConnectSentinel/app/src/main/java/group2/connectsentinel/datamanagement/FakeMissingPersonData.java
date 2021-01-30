package group2.connectsentinel.datamanagement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import group2.connectsentinel.data.MissingPerson;

// This class implements Singleton design.
public class FakeMissingPersonData implements MissingPersonDataSource {

    private LinkedList<MissingPerson> list;

    private static final MissingPersonDataSource fakeDB = new FakeMissingPersonData();
    private FakeMissingPersonData() {this.list = new LinkedList<MissingPerson>();}

    public static MissingPersonDataSource getInstance() {
        return fakeDB;
    }

    public void add(MissingPerson person) {
        list.add(0, person);
    }

    public List<MissingPerson> getFirst(int n) {
        Iterator<MissingPerson> iter = list.iterator();
        List<MissingPerson> ret = new ArrayList<MissingPerson>();
        int count = 0;
        while (iter.hasNext() && count <= n) {
            ret.add(iter.next());
            count++;
        }
        return ret;
    }

    public boolean deletePerson(MissingPerson person) {
        return list.remove(person);
    }
}

