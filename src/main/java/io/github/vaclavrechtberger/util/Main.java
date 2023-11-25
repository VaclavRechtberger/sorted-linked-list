package io.github.vaclavrechtberger.util;

import java.util.LinkedList;
import java.util.List;


public class Main {
    public static void main(String [] args) {

        /*List<String> sortedList = new SortedLinkedList<String>(Utils.createCaseInsensitiveNaturalOrderNullLastStringComparator());

        sortedList.add(null);
        sortedList.add("d");
        sortedList.add("a");
        sortedList.add("A");
        sortedList.add("c");
        sortedList.add(null);


        //sortedList.sort(Comparator.comparing(s -> isNull(s) ? "" : s));

        System.out.println(sortedList);
        //System.out.println(new Integer(10000).compareTo(new Integer(2000)));*/

        List<String> list = new LinkedList<>();

        list.add("A");
        list.add("B");

        System.out.println(list);

        list.add(2,"X");

        System.out.println(list);
    }
}
