package io.github.vaclavrechtberger.util;

import java.util.Comparator;
import java.util.function.Function;

public class Utils {
    public static final <E extends Comparable<E>> Comparator<E> createNaturalOrderNullFirstComparator() {
        return Comparator.nullsFirst(Comparator.naturalOrder());
    }

    public static final <E extends Comparable<E>> Comparator<E> createNaturalOrderNullLastComparator() {
        return Comparator.nullsLast(Comparator.naturalOrder());
    }

    public static final Comparator<String> createCaseInsensitiveNaturalOrderNullLastStringComparator() {
        return Comparator.nullsLast(Comparator.comparing(Function.identity(), String.CASE_INSENSITIVE_ORDER));
    }
}
