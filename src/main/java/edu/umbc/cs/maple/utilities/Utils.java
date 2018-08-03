package edu.umbc.cs.maple.utilities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    // credit to stackoverflow
    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    // credit to techie delight
    public static int indexOf(String[] strings, String target) {
        return IntStream.range(0, strings.length)
                .filter(i -> strings[i].equals(target))
                .findFirst()
                .orElse(-1);
    }

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
