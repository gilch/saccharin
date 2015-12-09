// Copyright 2015 Matthew Egan Odendahl
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.gilch.saccharin;

import com.github.gilch.saccharin.functional.Predicate;
import com.github.gilch.saccharin.sequential.*;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.github.gilch.saccharin.BuiltIn.in;
import static com.github.gilch.saccharin.BuiltIn.range;
import static com.github.gilch.saccharin.Literal._;
import static com.github.gilch.saccharin.Literal._a;

/**
 * Created by ME on 12/9/2015.
 */
public final class Lazy {
    private Lazy() {
        throw new UnsupportedOperationException();
    }

    /**
     * Makes an iterator that computes the Cartesian product, which is like a nested loop.
     * If the iterators from the arguments return elements in ascending order, then the pairs
     * returned are in lexicographic order. If the iterators return elements in descending
     * order, then the pairs are returned in reverse lexicographic order.
     *
     * @param iIt Iterator for the head element of the pair. It is only used once for the outer
     *            loop.
     * @param js  Iterable for the tail element of the pair. The js must be reusable if
     *            iIt has more than one element, because js are used in the inner loop.
     *            Do not create with in().
     * @param <I>
     * @param <J>
     * @return The an Iterator that produces the Cartesian product set (in lexicographic order
     * assuming the inputs are in order).
     */
    public static <I, J> Iterator<Literal._<I, J>> lexCart(
            final Iterator<? extends I> iIt, final Iterable<? extends J> js) {
        return new Generator<_<I, J>>() {
            @Override
            protected void generate() throws InterruptedException {
                for (final I i : in(iIt))
                    for (final J j : js)
                        yield(_(i, j));
            }
        }.start();
    }

    /**
     * Makes an iterator that computes the Cartesian product, which is like a nested loop.
     * If the iterators from the arguments return elements in ascending order, then the pairs
     * returned are in colexicographic order. If the iterators return elements in descending
     * order, then the pairs are returned in reverse colexicographic order.
     *
     * @param js  Iterable for the head element of the pair. The js must be reusable if
     *            iIt has more than one element, because js are used in the inner loop.
     *            Do not create with in().
     * @param iIt Iterator for the tail element of the pair. It is only used once for the outer
     *            loop.
     * @param <J>
     * @param <I>
     * @return The an Iterator that produces the Cartesian product set (in colexicographic order
     * assuming the inputs are in order).
     */
    public static <J, I> Iterator<_<J, I>> colexCart(
            final Iterable<? extends J> js, final Iterator<? extends I> iIt) {
        return new Generator<_<J, I>>() {
            @Override
            protected void generate() throws InterruptedException {
                for (final I i : in(iIt))
                    for (final J j : js)
                        yield(_(j, i));
            }
        }.start();
    }


    /**
     * Repeats an iterator, by saving an internal list.
     *
     * @param it
     * @param <E>
     * @return
     */
    public static <E> Iterator<E> cycle(final Iterator<? extends E> it) {
        return new Generator<E>() {
            @Override
            protected void generate() throws InterruptedException {
                final List<E> saved = _a();
                for (final E e : in(it)) {
                    yield(e);
                    saved.add(e);
                }
                while (saved.size() > 0) {
                    for (final E e : saved) yield(e);
                }
            }
        }.start();
    }

    public static <E> Iterator<E> repeat(final E e) {
        return new InfiniteSequence<E>() {
            @Override
            public E next() {
                return e;
            }
        };
    }

    public static <E> Iterator<E> repeat(final E e, final int times) {
        return new SequenceAdapter<E>() {
            int remaining = times;

            @Override
            public boolean hasNext() {
                return remaining > 0;
            }

            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                remaining--;
                return e;
            }
        };
    }

    /**
     * Concatenates iterators.
     *
     * @param links
     * @param <E>
     * @return
     */
    public static <E> Iterator<E> chain(final Iterator<? extends Iterator<? extends E>> links) {
        return new Generator<E>() {
            @Override
            protected void generate() throws InterruptedException {
                for (final Iterator<? extends E> link : in(links))
                    for (final E e : in(link))
                        yield(e);
            }
        }.start();
    }

    public static <E> Iterator<E> compress(
            final Iterator<? extends E> data, final Iterator<Boolean> selectors) {
        return new LookAheadSequence<E>() {
            @Override
            protected E getNext() throws NoSuchElementException {
                final E out = data.next();
                if (selectors.next()) return (out);
                else return getNext();
            }
        };
    }

    public static <E> Iterator<E> dropWhile(
            final Predicate<? super E> tester, final Iterator<? extends E> it) {
        return new Generator<E>() {
            @Override
            protected void generate() throws InterruptedException {
                final Iterable<? extends E> iter = in(it);//only consumed once.
                for (final E e : iter) {
                    if (!tester.test(e)) {
                        yield(e);
                        break;//didn't finish iter!
                    }
                }
                for (final E e : iter) yield(e); //finishes iter
            }
        }.start();
    }

    public static <E> Iterator<E> takeWhile(
            final Predicate<? super E> tester, final Iterator<? extends E> it) {
        return new LookAheadSequence<E>() {
            @Override
            protected E getNext() throws NoSuchElementException {
                final E e = it.next();
                if (tester.test(e)) return e;
                throw new NoSuchElementException();
            }
        };
    }

    public static <E> Iterator<E> iSlice(
            final Iterator<? extends E> it,
            final Integer start, final Integer stop, final Integer step) {
        return new Generator<E>() {
            @Override
            protected void generate() throws InterruptedException {
                final Iterator<Integer> slice = range(
                        start != null ? start : 0,
                        stop != null ? stop : Integer.MAX_VALUE,
                        step != null ? step : 1).iterator();
                int i = 0;
                int targetIndex = slice.next();
                for (final E e : in(it)) {
                    if (i++ == targetIndex) {
                        yield(e);
                        targetIndex = slice.next();
                    }
                }
            }
        }.start();
    }
}
