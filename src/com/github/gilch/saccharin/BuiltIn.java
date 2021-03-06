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

import com.github.gilch.saccharin.functional.Effect;
import com.github.gilch.saccharin.functional.Function;
import com.github.gilch.saccharin.functional.Predicate;
import com.github.gilch.saccharin.sequential.*;

import java.io.PrintStream;
import java.util.*;

import static com.github.gilch.saccharin.Literal._;
import static com.github.gilch.saccharin.Literal._t;

/**
 * Contains useful general-purpose static methods for use with import
 * static *
 *
 * @author Matthew Odendahl
 */
public final class BuiltIn {
    private BuiltIn() {
        throw new UnsupportedOperationException();
    }

    /**
     * A null of type VOID. When dealing with generics, it is often better to use VOID, which
     * is a (Void)null instead of a plain null which may be interpreted as a type not intended.
     */
    public static final Void VOID = null;

    /**
     * Platform-specific line separator as generated by String.format("%n")
     * This may or may not be preferable to "\n" in some cases.
     */
    public static final String LN = String.format("%n");

    /**
     * Counting generator. An infinite counting Iterable starting from 0
     */
    public static final Iterable<Integer> COUNT = count(0, 1);

    /**
     * Immutable (practically) publicly shared convenience {@code Random} that
     * lacks the {@code setSeed()} method. Attempting to call
     * {@code rand.setSeed()} will throw an exception. (Otherwise anyone could
     * set the publicly shared seed at any time and make the values non-random.)
     * This makes {@code RAND} practically immutable because even though the
     * "next" methods do technically change the state of a fake random
     * generator, in practice the output is, by design, unpredictable. The state
     * doesn't change the hash code or equality either.
     */
    public static final Random RANDOM = new Random() {
        private static final long serialVersionUID = 1L;
        private boolean isSet = false;

        @Override
        public synchronized void setSeed(final long seed)
                throws UnsupportedOperationException {
            // must initialize seed, but then it cannot be changed
            if (isSet) {
                throw new UnsupportedOperationException();
            }
            super.setSeed(seed);
            isSet = true;
        }

    };

    static {
        RANDOM.setSeed(new Random().nextLong());
    }

    /**
     * Counting generator.
     *
     * @param start -
     *              the first number of the Iterable
     * @return an infinite counting Iterable starting from <b>start</b>
     */
    public static Iterable<Integer> count(final int start) {
        if (start == 0) return COUNT;
        return count(start, 1);
    }

    /**
     * Counting generator.
     *
     * @param start -
     *              the first number of the sequence
     * @param step  -
     *              the number to count by; the number added each step.
     * @return an infinite counting Iterable starting from <b>start</b> and
     * incrementing by <b>step</b>
     */
    public static Iterable<Integer> count(final int start, final int step) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new InfiniteSequence<Integer>() {
                    private int count = start - step;

                    @Override
                    public Integer next() {
                        count += step;
                        return count;
                    }
                };
            }
        };
    }

    /**
     * Convert a Function to a Predicate. The function must
     * return Boolean.
     *
     * @param f a T, Boolean Function to convert to a Predicate
     * @return a predicate view of the Function.
     */
    public static <T> Predicate<T> toPredicate(final Function<? super T, Boolean> f) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return f.apply(t);
            }
        };
    }

    public static abstract class PrintRx extends Rx<Void, PrintRx> {
        @Override
        protected PrintRx getThis() {
            return this;
        }

        public final Term<PrintStream> stream = is(System.out);
        public final Term<String> separator = is(" ");
        public final Term<String> start = is("");
        public final Term<String> end = is(LN);
    }

    /**
     * Print a series of objects with customizable stream, separator, start, and end.
     *
     * @param args
     * @return
     */
    public static PrintRx printRx(final Object... args) {
        return new PrintRx() {
            @Override
            public Void go() {
                final PrintStream stream = this.stream._;
                stream.print(start._);
                {
                    final Itr<Object> itr;
                    for (final Object o : itr = from(_t(args))) {
                        stream.print(o);
                        stream.print(itr.hasNext() ? separator._ : "");
                    }
                }
                stream.print(end._);
                return VOID;
            }
        };
    }

    public static void print(final Object... args) {
        printRx(args).go();
    }

    public static abstract class JoinRx extends Rx<String, JoinRx> {
        @Override
        protected JoinRx getThis() {
            return this;
        }

        public final Term<String> separator = is(" ");
        public final Term<String> start = is("");
        public final Term<String> end = is("");
    }

    /**
     * Like printRx, but returns the string rather than printing it.
     *
     * @param args
     * @return
     */
    public static JoinRx joinRx(final Object... args) {
        return new JoinRx() {

            @Override
            public String go() {
                final StringBuilder string = new StringBuilder(start._);
                final String separator = this.separator._;
                {
                    final Itr<Object> itr;
                    for (final Object o : itr = from(_t(args))) {
                        string.append(str(o));
                        string.append(itr.hasNext() ? separator : "");
                    }
                }
                string.append(end._);
                return string.toString();
            }
        };
    }

    /**
     * Converts arrays of one reference type to another.
     *
     * @param newType  - the class of the copy to be returned
     * @param original - the array to be copied
     * @return a copy of the original Array as newType.
     * @throws ArrayStoreException  if an element copied from original is not of a runtime type
     *                              that can be stored in an array of class newType
     * @throws NullPointerException if original is null
     */
    public static <T, U> T[] convertArray(final Class<? extends T[]> newType, final U[] original) {
        return Arrays.copyOf(original, original.length, newType);
    }

    /**
     * Applies func cumulatively to initial with the items from iterable.
     * For example, a summing function could add a list of numbers to
     * initial. The initial need not be the same type as produced by the
     * iterable, for example, the initial could be a BigInteger and the
     * iterable could produce Longs.
     *
     * @param func     - the function to reduce the iterable
     * @param iterable - to provide the tail argument to func
     * @param initial  - to provide the head argument to func
     * @return a single value produced by cumulatively applying all items
     * from iterable to initial through func
     */
    public static <T, R> R reduce(
            final Function<_<? super R, ? extends T>, ? extends R> func,
            final R initial, final Iterable<? extends T> iterable) {
        final Iterator<? extends T> it = iterable.iterator();
        R out = initial;
        while (it.hasNext()) out = func.apply(_(out, it.next()));
        return out;
    }

    /**
     * Applies operator cumulatively to the previous result with every item
     * from iterable, starting with the first item from the iterator as
     * the first "result".
     *
     * @param operator - a Function representing a binary operator
     * @param iterable - provides the operands
     * @return the cumulative value.
     */
    public static <R> R reduce(
            final Function<_<? super R, ? extends R>, ? extends R> operator,
            final Iterable<? extends R> iterable) {
        final Iterator<? extends R> it = iterable.iterator();
        R out = it.next();
        while (it.hasNext()) out = operator.apply(_(out, it.next()));
        return out;
    }

    /**
     * if all true
     *
     * @param it - iterator producing Booleans
     * @return true if and only if none of the elements are false
     */
    public static boolean all(final Iterable<Boolean> it) {
        for (final boolean b : it) if (!b) return false;
        return true;
    }

    /**
     * if all true
     *
     * @param bs - booleans
     * @return true if and only if none of the arguments are false
     */
    public static boolean all(final boolean... bs) {
        for (final boolean b : bs) if (!b) return false;
        return true;
    }

    /**
     * if any true
     *
     * @param it - iterator producing Booleans
     * @return true if any of the elements are true
     */
    public static boolean any(final Iterable<Boolean> it) {
        for (final boolean b : it) if (b) return true;
        return false;
    }

    /**
     * if any true
     *
     * @param bs - booleans
     * @return true if any of the elements are true
     */
    public static boolean any(final boolean... bs) {
        for (final boolean b : bs) if (b) return true;
        return false;
    }

    /**
     * Safely performs a logical Object.equals(other) even if the first
     * object might be null.
     *
     * @param self  - the first object--equals is called on this object
     * @param other - the second object--equals is passed this object
     * @return true if both objects are null, or if self.equals(other)
     * returns true--false otherwise.
     */
    public static boolean eq(final Object self, final Object other) {
        return self == null ? other == null : self.equals(other);
    }

    /**
     * Safely calls toString() even if the Object might be null.
     *
     * @param o the Object toString
     * @return o.toString() or "null" if o is null.
     */
    public static String str(final Object o) {
        return o == null ? "null" : o.toString();
    }

    /**
     * Starts a cons stack with the specified element.
     * The tail for this cell is (Void)null.
     *
     * @param element
     * @param <E>
     * @return
     */
    public static <E> _<E, Void> stack(final E element) {
        return new _<E, Void>(element, VOID);
    }

    /**
     * Safely calls hashCode() even if the Object might be null.
     *
     * @param o the object to hash
     * @return o.hashCode() or 0 if o is null.
     */
    public static int hash(final Object o) {
        return o == null ? 0 : o.hashCode();
    }

    public static <R> Iterator<R> iter(final Effect<R> generator, final R sentinel) {
        return new LookAheadSequence<R>() {
            @Override
            protected R getNext() throws NoSuchElementException {
                final R out = generator.call();
                if (out == sentinel) throw new NoSuchElementException();
                return out;
            }
        };
    }

    /**
     * Wraps an iterator into an Iterable that returns the iterator.
     * This is typically used for a single foreach loop.
     * Iterables made this way can only be used once because they have no
     * way of regenerating the Iterator. If reuse is necessary, use
     * _a(Iterator) instead.
     *
     * @param it
     * @param <T>
     * @return
     */
    public static <T> Iterable<T> in(final Iterator<T> it) {
        return new Iterable<T>() {
            boolean started = false;

            @Override
            public Iterator<T> iterator() {
                if (started) {
                    throw new IllegalStateException(
                            "An Iterable made with in() may only be used once.");
                }
                started = true;
                return it;
            }
        };
    }

    //TODO: document better.

    /**
     * Captures the iterator from an iterable. Example:<br />
     * {@code
     * {Itr<String> itr;for(String s:itr=from(list)){
     * System.out.print(s+itr.hasNext()?", ":"\n");
     * }}
     * }
     *
     * @param it
     * @param <T>
     * @return
     */
    public static <T> Itr<T> from(final Iterable<T> it) {
        return new Itr<T>(it);
    }

    /**
     * Checks if none of the iterators are done.
     * Used for multiple iteration in a single loop. <br />
     * Example:<br />
     * <code>
     * Iterator<Integer> a = ints.iterator();<br />
     * Iterator<String> b = strings.iterator();<br />
     * for(int i=0;noneDone(a,b);i++) <br />
     * //use index i, a.next(), b.next() in the same loop<br />
     * </code>
     *
     * @param iterators - the iterators to check for hasNext()
     * @return true only if each iterator hasNext, false if any are done
     */
    public static boolean noneDone(final Iterator<?>... iterators) {
        for (final Iterator<?> it : iterators) if (!it.hasNext()) return false;
        return true;
    }

    /**
     * Pairs elements in order given.
     *
     * @param a - Iterator producing the head of the pairs
     * @param b - Iterator producing the tail of the pairs
     * @return an Iterator producing pairs.
     * Pairs are produced until a or b runs out of elements.
     */
    public static <A, B> Iterator<_<A, B>> zip(
            final Iterator<? extends A> a, final Iterator<? extends B> b) {
        return new SequenceAdapter<_<A, B>>() {
            @Override
            public boolean hasNext() {
                return noneDone(a, b);
            }

            @Override
            public _<A, B> next() {
                return _(a.next(), b.next());
            }

        };
    }

    /**
     * @param collection - collection to freeze
     * @return unmodifiable view of c
     * @see Collections#unmodifiableCollection(Collection)
     */
    public static <E> Collection<E> freeze(final Collection<? extends E> collection) {
        return Collections.unmodifiableCollection(collection);
    }

    /**
     * @param list - list to freeze
     * @return unmodifiable view of list
     * @see Collections#unmodifiableList(List)
     */
    public static <E> List<E> freeze(final List<? extends E> list) {
        return Collections.unmodifiableList(list);
    }

    /**
     * @param map - map to freeze
     * @return unmodifiable view of m
     * @see Collections#unmodifiableMap(Map)
     */
    public static <K, V> Map<K, V> freeze(final Map<? extends K, ? extends V> map) {
        return Collections.unmodifiableMap(map);
    }

    /**
     * @param set - set to freeze
     * @return unmodifiable view of s
     * @see Collections#unmodifiableSet(Set)
     */
    public static <E> Set<E> freeze(final Set<? extends E> set) {
        return Collections.unmodifiableSet(set);
    }

    /**
     * @param map - SortedMap to freeze
     * @return unmodifiable view of m
     * @see Collections#unmodifiableSortedMap(SortedMap)
     */
    public static <K, V> SortedMap<K, V> freeze(final SortedMap<K, ? extends V> map) {
        return Collections.unmodifiableSortedMap(map);
    }

    /**
     * @param set - SortedSet to freeze
     * @return unmodifiable view of s
     * @see Collections#unmodifiableSortedSet(SortedSet)
     */
    public static <E> SortedSet<E> freeze(final SortedSet<E> set) {
        return Collections.unmodifiableSortedSet(set);
    }

    /**
     * Number sequence generator.
     *
     * @param start -
     *              the first number you want
     * @param stop  -
     *              the first number you don't want
     * @param step  -
     *              the increment length. Must be negative to decrement. Cannot
     *              be zero.
     * @return Range instance
     */
    public static Range range(final int start, final int stop, final int step) {
        return new Range(start, stop, step);
    }

    /**
     * Number sequence generator. The default increment is 1.
     *
     * @param start -
     *              the first number you want
     * @param stop  -
     *              the first number you don't want
     * @return Range instance
     */
    public static Range range(final int start, final int stop) {
        return new Range(start, stop, 1);
    }

    /**
     * Number sequence generator. The default start is 0, default increment
     * is 1.
     *
     * @param stop -
     *             the first number you don't want
     * @return Range instance
     */
    public static Range range(final int stop) {
        return new Range(0, stop, 1);
    }

    /**
     * Makes a Range the length of the collection. Equivalent to
     * {@code range(c.length())} Useful for a hybrid for-each loop with index:
     * <p/>
     * The common idiom {@code for(int i=0;i<c.length;i++)} becomes the much
     * cleaner and less error-prone {@code for(int i:range(c)) }
     *
     * @param c -
     *          the collection to generate a Range for.
     * @return Range instance from 0 to c.length()-1
     */
    public static Range range(final Collection<?> c) {
        return range(c.size());
    }

    /**
     * Makes a range the length of the array. Equivalent to
     * {@code range(a.length)} Useful for a hybrid for-each loop with index:
     * <p/>
     * The common idiom {@code for(int i=0;i<a.length;i++)} becomes the much
     * cleaner and less error-prone {@code for(int i:range(a)) }
     *
     * @param a -
     *          an array.
     * @return Range instance from 0 to a.length-1
     */
    public static Range range(final Object[] a) {
        return range(a.length);
    }

    /**
     * An unmodifiable List designed to mimic the functionality of a typical for loop,
     * but using the less error-prone "for each" syntax in most situations where
     * an integer for loop is required. It doesn't actually store the list of
     * numbers, but generates them on the fly.
     *
     * @author Matthew Odendahl
     */
    public static class Range extends AbstractList<Integer> {
        // generator fields.
        private final int offset;
        private final int step;
        /**
         * The number of elements in this Range
         */
        public final int length;

        private Range(final int start, final int stop, final int step) {
            offset = start;
            this.step = step;

            // may need to be scaled.
            int size = (step < 0) ? start - stop : stop - start;
            // add step if there's a remainder
            final int scale = Math.abs(step);
            size += size % scale > 0 ? scale : 0;
            // integer divide should scale properly now
            size /= scale;
            length = (size > 0) ? size : 0;
        }

        /**
         * sub is short for subscript. Identical to get(int) but returns a primitive int instead of
         * a boxed Integer. This method is preferable to get(int) because it avoids the boxing step,
         * but it doesn't conform to the List interface, which is generic and can't return
         * primitives.
         *
         * @param i
         * @return
         */
        public int sub(final int i) {
            return i * step + offset;
        }

        @Override
        public Integer get(final int i) {
            return sub(i);
        }

        @Override
        public int size() {
            return length;
        }
    }

//    // j-like verb trains?
//
//    public static <A,B,T,R> Saccharin.Function<T,R> fork(
//            final Saccharin.Function<?super T,?extends A> left,
//            final Saccharin.Function<_<?extends A,?extends B>,?extends R> splice,
//            final Saccharin.Function<?super T,?extends B> right)
//    {
//        return new Saccharin.Function<T, R>() {
//            @Override public R apply(T t) {
//                return splice.apply(_(left.apply(t), right.apply(t)));
//            }
//        };
//    }
//
//    public static <A,T,R> Saccharin.Function<T,R> hook1(
//            final Saccharin.Function<_<?super T,?extends A>,?extends R> splice,
//            final Saccharin.Function<?super T,?extends A> right)
//    {
//        return new Saccharin.Function<T, R>() {
//            @Override public R apply(T t) {
//                return splice.apply(_(t, right.apply(t)));
//            }
//        };
//    }
//
//    public static <A,H,T,R> Saccharin.Function<_<H,T>,R> hook2(
//            final Saccharin.Function<_<?super H,?extends A>,?extends R> splice,
//            final Saccharin.Function<?super T,?extends A> right)
//    {
//        return new Saccharin.Function<_<H,T>, R>() {
//            @Override public R apply(_<H,T> pair) {
//                return splice.apply(_(pair.head, right.apply(pair.tail)));
//            }
//        };
//    }
}
