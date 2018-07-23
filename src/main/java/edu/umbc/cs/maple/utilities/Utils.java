package edu.umbc.cs.maple.utilities;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Utils {

    // credit to stackoverflow
    public static <E> E choice(Collection<? extends E> coll, Random rand) {
        if (coll.size() == 0) {
            return null;
        }

        int index = rand.nextInt(coll.size());
        if (coll instanceof List) {
            return ((List<? extends E>) coll).get(index);
        } else {
            Iterator<? extends E> iter = coll.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

}
