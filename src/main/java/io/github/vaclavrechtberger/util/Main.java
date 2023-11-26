package io.github.vaclavrechtberger.util;

import java.util.List;


public class Main {
    public static void main(String [] args) {

        List<String> list = new SortedLinkedList<>(Utils.createCaseInsensitiveNaturalOrderNullLastStringComparator());

        list.add("b");
        System.out.println(list);
        list.add("a");
        System.out.println(list);
        list.add("c");
        System.out.println(list);
        list.add("B");
        System.out.println(list);

        List<String> reverse = list.reversed();

        list.add("X");
        System.out.println(list);
        System.out.println(reverse);
    }
}
