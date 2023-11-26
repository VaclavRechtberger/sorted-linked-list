package io.github.vaclavrechtberger.util;

import java.util.*;
import java.util.stream.IntStream;


/**
 * Implementation of {@link List} interface.
 * The added values are automatically sorted.
 * According to this regards behaviour of this class slightly differ form {@link List} specification as mentioned below.
 * For instance added values are not appended to the end of this list but placed at appropriate position @see {@link io.github.vaclavrechtberger.util.SortedLinkedList#add(Comparable)}.
 * For more differences see whole documentation of this class and its methods.
 * This class is not thread-safe.
 * Note: To make this class thread-safe, we must place code in methods such as add, addFirst, addLast, remove, and removeAll within a synchronized block, utilizing the same lock, or implement some alternative upgrade for concurrent access.
 *
 * @author Vaclav Rechtberger
 * @author vaclav.rechtberger@gmail.com
 * @version 1.0
 */
public class SortedLinkedList<E extends Comparable<E>> implements List<E>{
    public static final String WRONG_POSITION_MESSAGE_TEMPLATE = "Cannot add specified %s at %s position since it would break ordering.";

    public static final String WRONG_REPLACEMENT_MESSAGE = "Cannot replace element at specified index by specified element since it would break ordering.";
    private final List<E> delegatedList = new LinkedList<>();

    private final Comparator<? super E> comparator;

    /**
     * Constructs an empty sorted list with natural ordering, placing null values at the beginning.
     */
    public SortedLinkedList() {
        this(Utils.createNaturalOrderNullFirstComparator());
    }

    /**
     * Constructs a sorted list with natural ordering, placing null values at the beginning, and containing the elements of the specified collection.
     * @param collection the specified collection
     */
    public SortedLinkedList(Collection<? extends E> collection) {
        this(collection, Utils.createNaturalOrderNullFirstComparator());
    }

    /**
     * Constructs an empty sorted list with the ordering given by the specified comparator.
     * Keep in mind that the specified comparator affects the behavior of this class
     * (e.g., if {@link java.util.Comparator#compare(Object, Object)} throws some {@link java.lang.Throwable},
     * then {@link io.github.vaclavrechtberger.util.SortedLinkedList#add(Comparable)} will throw this exception since the comparator is used in this method).
     * The ability of the comparator to handle null values will affect the behavior of this list, particularly in the adding of new values.
     * See {@link io.github.vaclavrechtberger.util.Utils}.
     * @param comparator the comparator to determine the ordering of elements
     */
    public SortedLinkedList(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * Constructs a sorted list with natural ordering, placing null values at the beginning, and containing the elements of the specified collection.
     * See {@link io.github.vaclavrechtberger.util.SortedLinkedList#SortedLinkedList(Comparator)}
     *
     * @param collection the specified collection
     * @param comparator the comparator to determine the ordering of elements
     */
    public SortedLinkedList(Collection<? extends E> collection, Comparator<E> comparator) {
        delegatedList.addAll(collection);
        delegatedList.sort(comparator);
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return delegatedList.size();
    }

    @Override
    public boolean isEmpty() {
        return delegatedList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegatedList.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return delegatedList.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegatedList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegatedList.toArray(a);
    }

    /**
     * Adds the specified element at the appropriate position in this list.
     * If this list already contains one or more equal elements, it appends the specified element to the end of this sublist of equal elements
     * (i.e., appends the specified element before the first larger element or at the end).
     *
     * @param e the element whose presence in this collection is to be ensured
     * @return true if the element was added
     */
    @Override
    public boolean add(E e) {
        if (isEmpty()) {
            return delegatedList.add(e);
        }
        Iterator<E> iterator = this.iterator();
        OptionalInt index = IntStream.range(0, this.size())
                .filter(i -> comparator.compare(iterator.next(), e) > 0)
                .findFirst();

        if (index.isPresent()) {
            delegatedList.add(index.getAsInt(), e);
            return true;
        } else { // list is empty or all the elements are less or equal
            return delegatedList.add(e);
        }
    }

    @Override
    public boolean remove(Object o) {
        return delegatedList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegatedList.containsAll(c);
    }

    /**
     * Inserts each element of the specified collection at the appropriate position in this list in the order they are returned by the specified collection's iterator.
     * Note: This implementation aligns with the idea of {@link io.github.vaclavrechtberger.util.SortedLinkedList#add(Comparable)}
     * but contradicts the idea of {@link io.github.vaclavrechtberger.util.SortedLinkedList#addAll(int, Collection)}
     * since that method could be understood as a shortcut to this method where {@code index = this.size()} (i.e., appends all the elements at the end of this list).
     *
     * @param c the collection containing elements to be added to this collection
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection contains one or more null elements and this list does not permit null elements, or if the specified collection is null
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return true;
    }

    /**
     * Inserts the specified elements at the specified position in this list in the appropriate order.
     * The following criteria must hold; otherwise, a {@link java.lang.IllegalArgumentException} will be thrown:
     * - {@code index == this.size()} or the element at the position of the specified index must be greater or equal to the largest element of the specified collection
     * - {@code index == 0} or the element at the position of the specified index - 1 must be less or equal to the smallest element of the specified collection
     *
     * @param index the index at which to insert the specified collection (after sorting)
     * @param c the collection containing elements to be inserted into this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one or more null elements and this list does not permit null elements, or if the specified collection is null
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBounds(index);
        if (c.isEmpty()) {
            return true;
        }
        List<E> tmpList = new ArrayList<>(c);
        tmpList.sort(this.comparator);
        if (checkForInsert(index, tmpList.getFirst(), tmpList.getLast())) {
            return delegatedList.addAll(index, tmpList);
        }
        throw new IllegalArgumentException(WRONG_POSITION_MESSAGE_TEMPLATE.formatted("collection", "specified"));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegatedList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegatedList.retainAll(c);
    }

    @Override
    public void clear() {
        delegatedList.clear();
    }

    @Override
    public E get(int index) {
        return delegatedList.get(index);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element if this action does not break the order.
     *
     * @param index the index of the element to replace
     * @param element the element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws NullPointerException if the specified element is null and
     *         this list does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list (e.g., order given by comparator)
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    @Override
    public E set(int index, E element) {
        checkBounds(index);
        if (checkForSet(index, element)) {
            return delegatedList.set(index, element);
        }
        throw new IllegalArgumentException(WRONG_REPLACEMENT_MESSAGE);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * The following criteria must hold; otherwise, a {@link java.lang.IllegalArgumentException} will be thrown:
     * - {@code index == 0} or the element at the position of the specified index - 1 must be less or equal to the specified element
     * - {@code index == this.size()} or the element at the position of the specified index must be greater or equal to the specified element
     *
     * @param index the index at which to insert the specified element (after sorting)
     * @param element the element to be inserted at the specified index
     * @return {@code true} if this list changed as a result of the call
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and this list does not permit null elements
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    @Override
    public void add(int index, E element) {
        checkBounds(index);
        if (checkForInsert(index, element)) {
            delegatedList.add(index, element);
            return;
        }
        throw new IllegalArgumentException(WRONG_POSITION_MESSAGE_TEMPLATE.formatted("element", "specified"));
    }

    @Override
    public E remove(int index) {
        return delegatedList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegatedList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegatedList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return delegatedList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return delegatedList.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return delegatedList.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return delegatedList.toString();
    }


    /**
     * Returns this list in reverse order. Due to incompatibility issues arising from ordering, the returned list is non-modifiable.
     * Note: The returned list can still be modified through this list. This issue will be resolved in future updates.
     */
    @Override
    public List<E> reversed() {
        return Collections.unmodifiableList(delegatedList.reversed());
    }

    /**
     * Unsupported operation since ordering is already given, and reordering could/would break this order. Throws {@link UnsupportedOperationException}.
     * Note: There are other alternatives on the table for implementing this method.
     * - Set the selected comparator, reorder elements, and use it from now onwards.
     * - Sort only subsequences of equivalent elements using the selected comparator in the context of the actual comparator (i.e., use it as a second-level sorting).
     *
     * @param comparator the comparator for sorting
     * @throws UnsupportedOperationException since this operation goes against the intent of this class
     */
    @Override
    public void sort(Comparator<? super E> comparator) {
        throw new UnsupportedOperationException("This class is sorted by nature and cannot be reordered.");
    }

    private void checkBounds(int index) {
        if (!isInBounds(index))
            throw new IndexOutOfBoundsException(outOfBoundsMessage(index));
    }

    private boolean isInBounds(int index) {
        return index >= 0 && index <= size();
    }

    private String outOfBoundsMessage(int index) {
        return "Index: " + index + ", Size: " + size();
    }

    /**
     * Checks whether the element can be inserted at the position given by the selected index.
     *
     * @param index the index at which to check insertion
     * @param element the element to be inserted
     * @return {@code true} if the element can be inserted at the position
     */
    private boolean checkForInsert(int index, E element) {
        return checkForInsert(index, element, element);
    }

    /**
     * Checks whether the sequence represented by its min and max elements can be inserted at the position given by the selected index.
     *
     * @param index the index at which to check insertion
     * @param min the minimum value to be checked
     * @param max the maximum value to be checked
     * @return {@code true} if the sequence represented by min and max values can be inserted at the position
     */
    private boolean checkForInsert(int index, E min, E max) {
        return (index == 0 || isLessOrEqual(get(index - 1), min)) && (index == size()  ||  isLessOrEqual(max, get(index)));
    }

    /**
     * Checks whether the element at the selected index can be replaced by the selected element without breaking the order.
     *
     * @param index the index of the element to be replaced
     * @param element the element for replacement
     * @return {@code true} if the element can be replaced without breaking the order
     */
    private boolean checkForSet(int index, E element) {
        return (index == 0 || isLessOrEqual(get(index - 1), element)) && (index == (size() - 1)  ||  isLessOrEqual(element, get(index + 1)));
    }

    /**
     * Checks whether e1 is less than or equal to e2.
     *
     * @param e1 the first element
     * @param e2 the second element
     * @return {@code true} if e1 <= e2
     */
    private boolean isLessOrEqual(E e1, E e2) {
        return comparator.compare(e1, e2) <= 0;
    }
}
