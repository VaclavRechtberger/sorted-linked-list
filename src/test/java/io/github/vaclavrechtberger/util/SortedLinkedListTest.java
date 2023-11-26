package io.github.vaclavrechtberger.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SortedLinkedListTest {

    @Test
    public void reversedListIsUnmodifiableTest() {
        SortedLinkedList sortedLinkedList = new SortedLinkedList();
        List reversed = sortedLinkedList.reversed();
        assertThrows(UnsupportedOperationException.class, () -> reversed.add(new Object()));
    }

    @Test
    public void sortMethodUnsupportedTest() {
        assertThrows(UnsupportedOperationException.class, () -> new SortedLinkedList().sort(null));
    }

    @ParameterizedTest
    @MethodSource("addParametersSource")
    public <E extends Comparable<E>> void addTest(E [] elementsToAdd, E [] expectedResult) {
        SortedLinkedList<E> sortedLinkedList = new SortedLinkedList<>();
        Arrays.stream(elementsToAdd).forEach(sortedLinkedList::add);
        assertThat(sortedLinkedList, contains(expectedResult));
    }
    public static Stream<Arguments> addParametersSource() {
        return Stream.of(
                Arguments.of(new String [] {"a", "c", "b"}, new String [] {"a", "b", "c"}),
                Arguments.of(new Integer [] {2, 1, 0}, new Integer [] {0, 1, 2}),
                Arguments.of(new Integer [] {6, -5, -1, null}, new Integer [] {null, -5, -1, 6})
        );
    }

    @Test
    public void addAtSubsequenceEndTest() {
        SortedLinkedList<String> sortedLinkedList = new SortedLinkedList<>(Utils.createCaseInsensitiveNaturalOrderNullLastStringComparator());
        sortedLinkedList.add("a");
        sortedLinkedList.add("b");
        sortedLinkedList.add("c");
        sortedLinkedList.add("B");
        assertThat(sortedLinkedList, contains("a", "b", "B", "c"));
    }

    @ParameterizedTest
    @MethodSource("insertParametersSource")
    public void insertElementAtRightPositionTest(List<String> initList, int index, String element, List<String> expectedResult) {
        SortedLinkedList<String> sortedLinkedList = new SortedLinkedList<>(initList);
        sortedLinkedList.add(index, element);
        assertThat(sortedLinkedList, contains(expectedResult.toArray()));
    }

    public static Stream<Arguments> insertParametersSource() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "c"), 0, "a", List.of("a", "a", "b", "c")),
                Arguments.of(List.of("a", "b", "c"), 1, "a", List.of("a", "a", "b", "c")),
                Arguments.of(List.of("a", "a", "b", "c"), 1, "a", List.of("a", "a", "a", "b", "c")),
                Arguments.of(List.of("a", "b", "c"), 3, "c", List.of("a", "b", "c", "c"))
        );
    }

    @ParameterizedTest
    @MethodSource("insertWrongPlaceParametersSource")
    public void insertElementAtWrongPlacePositionTest(List<String> initList, int index, String element) {
        SortedLinkedList<String> sortedLinkedList = new SortedLinkedList<>(initList);
        assertThrows(IllegalArgumentException.class, () -> sortedLinkedList.add(index, element));
    }

    public static Stream<Arguments> insertWrongPlaceParametersSource() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "c"), 0, "b"),
                Arguments.of(List.of("a", "b", "c"), 1, "c"),
                Arguments.of(List.of("a", "a", "b", "c"), 3, "a")
        );
    }

    @ParameterizedTest
    @MethodSource("setParametersSource")
    public void setElementAtRightPositionTest(List<String> initList, int index, String element, List<String> expectedResult) {
        SortedLinkedList<String> sortedLinkedList = new SortedLinkedList<>(initList, Utils.createCaseInsensitiveNaturalOrderNullLastStringComparator());
        sortedLinkedList.set(index, element);
        assertThat(sortedLinkedList, contains(expectedResult.toArray()));
    }

    public static Stream<Arguments> setParametersSource() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "d"), 1, "c", List.of("a", "c", "d")),
                Arguments.of(List.of("a", "B", "c"), 1, "b", List.of("a", "b", "c")),
                Arguments.of(List.of("a", "a", "a"), 1, "A", List.of("a", "A", "a"))
        );
    }

    @ParameterizedTest
    @MethodSource("setWrongParametersSource")
    public void setElementAtWrongPositionTest(List<String> initList, int index, String element) {
        SortedLinkedList<String> sortedLinkedList = new SortedLinkedList<>(initList, Utils.createCaseInsensitiveNaturalOrderNullLastStringComparator());
        assertThrows(IllegalArgumentException.class, () -> sortedLinkedList.set(index, element));
    }

    public static Stream<Arguments> setWrongParametersSource() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "d"), 1, "e"),
                Arguments.of(List.of("a", "B", "c"), 0, "c"),
                Arguments.of(List.of("a", "b", "b"), 2, "A")
        );
    }

    @Test
    public void reversedListIsEditableThroughOriginalList() {
        List<String> original = new SortedLinkedList<>(List.of("aaa", "bbb", "ccc"));
        List<String> reversed =  original.reversed();
        original.add("ddd");
        assertThat(reversed.getFirst(), is("ddd"));
        assertThat(reversed.getLast(), is("aaa"));
    }

    @Test
    public void canBeStreamed() {
        assertThat(new SortedLinkedList<>(List.of("b", "a")).stream().toList(),
                contains(List.of("a", "b").toArray()));
    }
}