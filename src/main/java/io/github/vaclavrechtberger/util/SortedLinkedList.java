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
 * Note: to make this class thread-safe we must place code in methods like add, addFirst, addLast, remove and removeAll in synchronized block with same lock;
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
     * Constructs an empty sorted list with natural ordering and null values first.
     */
    public SortedLinkedList() {
        this(Utils.createNaturalOrderNullFirstComparator());
    }

    /**
     * Constructs a sorted list with natural ordering and null values first containing the elements of the specified.
     * @param collection
     */
    public SortedLinkedList(Collection<? extends E> collection) {
        this(collection, Utils.createNaturalOrderNullFirstComparator());
    }

    /**
     * Constructs an empty sorted list with ordering given by specified comparator.
     * Keep in mind that specified comparator affects behaviour of this class
     * (e.g. if {@link java.util.Comparator#compare(Object, Object)} throws some {@link java.lang,Throwable} then {@link io.github.vaclavrechtberger.util.SortedLinkedList#add(Comparable)}
     * will throw this since the comparator is used in this method).
     * The ability of comparator to handle null values will affect behaviour of this list, namely adding of new values.
     * See {@link io.github.vaclavrechtberger.util.Utils}.
     */
    public SortedLinkedList(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * Constructs a sorted list with natural ordering and null values first containing the elements of the specified collection.
     * See {@link io.github.vaclavrechtberger.util.SortedLinkedList#SortedLinkedList(Comparator)}
     * @param collection
     * @param comparator
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
     * Adds the specified element at appropriate position in this list.
     * If this list already contains one or more equal elements, then it appends the specified element to the end of this sublist of equal elements
     * (e.i. appends specified element before first larger element or at the end)
     * @param e element whose presence in this collection is to be ensured
     * @return true if elements were added
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
     * Inserts each element of the specified collection at appropriate position of this list in order that they are returned by the specified collection's iterator.
     * Note: This implementation is aligned with the idea of {@link io.github.vaclavrechtberger.util.SortedLinkedList#add(Comparable)}
     * but goes against idea of {@link io.github.vaclavrechtberger.util.SortedLinkedList#addAll(int, Collection)}
     * since this method could be understood as shortcut to this method where {@code index = this.size()} (e.i. appends all the elements at the end of this list)
     * @param c collection containing elements to be added to this collection
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection contains one or more null elements and this list does not permit null elements, or if the specified collection is null
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return true;
    }

    /**
     * Adds the specified elements at specified position in this list.
     * Following criteria must hold otherwise {@link java.lang.IllegalArgumentException} will be thrown:
     * - c is empty or {@code null}
     * - {@code index == this.size()} or element at position of the specified index must be greater or equal to the largest element of the specified collection
     * - {@code index == 0} or element at position of the specified index - 1 must be less or equal to the smallest element of the specified collection
     * @param index index at which to insert the specified collection (after sort)
     * @param c collection containing elements to be inserted to this list
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
     * specified element if this action do not break order.
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws NullPointerException if the specified element is null and
     *         this list does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list (e.g. order given by comparator)
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
     * Adds the specified element at specified position in this list.
     * Following criteria must hold otherwise {@link java.lang.IllegalArgumentException} will be thrown:
     * - {@code index == 0} or element at position of the specified index - 1 must be less or equal to the specified element
     * - {@code index == this.size()} or element at position of the specified index must be greater or equal to the specified element
     * @param index index at which to insert the specified collection (after sort)
     * @param element to be inserted at index
     * @return {@code true} if this list changed as a result of the call
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one or more null elements and this list does not permit null elements, or if the specified collection is null
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
     * Returns this list in reverse order. For some incompatibility issues given by ordering the returned list is not non-modifiable.
     * Note: returned list is still modifiable via this list. This issue will be resolved further.
     */
    @Override
    public List<E> reversed() {
        return Collections.unmodifiableList(delegatedList.reversed());
    }

    /**
     * Unsupported operation since ordering is already given and reordering could/would break this order. Throws {@link UnsupportedOperationException}.
     * Note: There are other alternatives on the table how to implement this method.
     * - set selected comparator, reorder elements and use it from now onwards
     * - sort using selected comparator only subsequences of equivalent elements in the context of actual comparator (e.i. use it as second level sorting)
     * @param comparator comparator for sorting
     * @throws UnsupportedOperationException since this operation goes against intent of this class
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
     * Checks whether element can be inserted at position given by selected index.
     * @param index
     * @param element
     * @return {@code true} if element can be inserted at position
     */
    private boolean checkForInsert(int index, E element) {
        return checkForInsert(index, element, element);
    }

    /**
     * Checks whether sequence represented by its min and max element can be inserted at position given by selected index.
     * @param index
     * @param min value to be checked
     * @param max value to be checked
     * @return {@code true} if sequence represented by min and max values can be inserted at position
     */
    private boolean checkForInsert(int index, E min, E max) {
        return (index == 0 || isLessOrEqual(get(index - 1), min)) && (index == size()  ||  isLessOrEqual(max, get(index)));
    }

    /**
     * Checks whether element at selected index can be replaced by selected element without breaking the order.
     * @param index of replaced element
     * @param element for replacement
     * @return {@code true} if eleemnt can be replaced without breaking the order.
     */
    private boolean checkForSet(int index, E element) {
        return (index == 0 || isLessOrEqual(get(index - 1), element)) && (index == (size() - 1)  ||  isLessOrEqual(element, get(index + 1)));
    }

    /**
     * Checks whether e1 is less or equal then e2.
     * @param e1
     * @param e2
     * @return {@code true} if e1 <= e2
     */
    private boolean isLessOrEqual(E e1, E e2) {
        return comparator.compare(e1, e2) <= 0;
    }
}
