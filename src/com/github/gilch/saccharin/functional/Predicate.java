package com.github.gilch.saccharin.functional;

import com.github.gilch.saccharin.sequential.LookAheadSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Unlike Function<T,Boolean>, this won't return null.
 * Predicates have the powerful filter method
 *
 * @param <T>
 */
public abstract class Predicate<T> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t - the input argument
     * @return true if the input argument matches the predicate,
     * otherwise false
     */
    public abstract boolean test(T t);

    /**
     * Construct a list from those elements of the iterable which pass
     * test(T)
     *
     * @param iterable - items to filter
     * @return a list containing the items produced by iterable (in order)
     * except for the items which returned false when tested.
     */
    public final List<T> filter(final Iterable<? extends T> iterable) {
        final ArrayList<T> out = new ArrayList<T>();
        for (final T t : iterable) if (test(t)) out.add(t);
        return out;
    }

    /**
     * Lazy version of filter which filters an iterator by testing each returned element.
     * This is safe to use with infinite Iterators.
     *
     * @param iterator
     * @return
     */
    public final Iterator<T> filter(final Iterator<? extends T> iterator) {
        return new LookAheadSequence<T>() {
            @Override
            protected T getNext() throws NoSuchElementException {
                while (iterator.hasNext()) {
                    final T next = iterator.next();
                    if (test(next)) return next;
                }
                throw new NoSuchElementException();
            }
        };
    }

    /**
     * A Function view of this predicate.
     */
    public final Function<? super T, Boolean> toFunction() {
        return new Function<T, Boolean>() {
            @Override
            public Boolean apply(final T t) {
                return test(t);
            }
        };
    }

    /**
     * @return an inverted view of this predicate
     */
    public final Predicate<T> negate() {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return !Predicate.this.test(t);
            }
        };
    }

}
