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

import com.github.gilch.saccharin.functional.Function;

import java.lang.reflect.Array;
import java.util.*;

import static com.github.gilch.saccharin.BuiltIn.*;

/**
 * Use <br />
 * {@code import static com.github.gilch.saccharin.Literal.*;}
 * <br /> to access convenient initializer syntax methods for some common
 * data structures, including tuple, array, set, deque, dynamic array, map,
 * and cons cell. These may look like new syntax, but are actually just
 * method calls using short method names that begin with an underscore "_"
 * plus clever use of varargs and chained methods returning "this".
 * <br /><br />
 * <code>
 * _(1,"one") //cons cell<br />
 * _(1f,_(2d,_('3',VOID))) //cons linked list<br />
 * _0b("1010"+"0100") // binary (long)<br />
 * _t(1,2,3) //tuple (array as List)<br />
 * _f(1,2,3) //frozen tuple (array as unmodifiable List)<br />
 * _q(1,2,3) //double-ended queue<br />
 * _a(1,2,3) //dynamic array<br />
 * items("foo","bar") //reference array<br />
 * _s(1,2,3) //linked hash set<br />
 * _x("one",1)._("two",2)._("three",3) //Map<br />
 * </code>
 * Primitives arrays are also available, except for byte[] and short[], which
 * are arguably better created with Java's native syntax, since a
 * fake literal method could not do this efficiently.
 *
 * @author Matthew Odendahl
 */
public final class Literal {

    private Literal() {
        throw new UnsupportedOperationException();
    }

    /**
     * A simple public alias for cons(head,tail). Used to quickly construct
     * statically-checked, mixed-type, cons-cell data structures (or
     * constructs) without the syntactic overhead of creating a new class.
     * Examples:<br />
     * Linked stack: _(a,_(b,_(c,_(d,VOID)))).<br />
     * Tree: _(_(a,b),_(c,d))<br />
     * Hint: copy and paste "_(,)", then move the cursor back with
     * the left arrow key and paste again when declaring nested constructs.
     *
     * @param head - object to store in head
     * @param tail - object to store in tail
     * @return a construct _(head, tail)
     */
    public static <H, T> _<H, T> _(final H head, final T tail) {
        return new _<H, T>(head, tail);
    }

    /**
     * Terminating Cons cell for linked stack. The tail is null, of type
     * Void. This overload is preferable to the generic _(head, null) that
     * it replaces, which would have made the null an Object by default.
     * It is also shorter than the correct _(head,(Void)null) would have been.
     * Void types are explicitly meant to be null--as
     * they cannot be instantiated (except via reflection)--while Object
     * types could be anything. Nulls of other types are still possible, but may require an
     * explicit cast.
     *
     * @param head - terminating element.
     * @param tail - null
     * @return a construct containing _(head, (Void) null)
     */
    public static <H> _<H, Void> _(final H head, final Void tail) {
        return new _<H, Void>(head, tail);
    }

    public static <T> _<Void, T> _(final Void head, final T tail) {
        return new _<Void, T>(head, tail);
    }

    public static _<Void, Void> _(final Void head, final Void tail) {
        return new _<Void, Void>(head, tail);
    }

    /**
     * Out fake literal.
     *
     * @return a new instance of {@link Out}
     */
    public static <T> Out<T> _() {
        return new Out<T>();
    }

    /**
     * Thru fake literal.
     *
     * @param t
     * @return a new instance of {@link Thru}
     * with t as the referent.
     */
    public static <T> Thru<T> _(final T t) {
        return new Thru<T>(t);
    }

    /**
     * Binary long fake literal. Obsolete since Java 7, but useful in legacy versions.
     *
     * @param binary - must be of the form "-?[01][01]*", but no longer than
     *               what will fit in a 64-bit singed long. Negative numbers will be
     *               converted to two's complement. Digits may be grouped with the "+"
     *               operator, for example: {@code _0b("1000"+"0011") == 0x83L}
     * @return the long value of the binary string. Use an explicit
     * truncating cast for smaller primitives.
     * @throws NumberFormatException This can't be checked at compile time
     *                               like a true literal.
     *                               It is advisable to only use binary fake literals as
     *                               {@code static final} constants so the exceptions is thrown
     *                               immediately during runtime.
     */
    public static long _0b(final String binary) {
        return Long.valueOf(binary, 2);
    }

    /**
     * Dynamic array fake literal. Unlike normal arrays, dynamic arrays
     * can be resized, but retain array-like performance most of the time.
     * This method will cause issues when creating a varargs array with unchecked generic
     * elements--Use the single element method with the {@code ._(e)} chain syntax instead.
     *
     * For example, use {@code _A<Thru<Float>> foo = _a(_(1f))._(_(2f));}
     * instead of {@code _A<Thru<Float>> foo = _a(_(1f),_(2f));}.
     *
     * @param e -
     *          an Array of type E. Multiple arguments will be converted
     *          to an array automatically.
     * @return a dynamically-sizable {@link ArrayList} with the provided
     * elements in the order given.
     */
    //@SafeVarargs
    public static <E> _A<E> _a(final E... e) {
        return new _A<E>(_t(e));
    }

    /**
     * Dynamic array fake literal with a single starting element. Chain with ._() to add more
     * elements.
     *
     * @param e
     * @param <E>
     * @return
     */
    public static <E> _A<E> _a(final E e) {
        return new _A<E>()._(e);
    }

    /**
     * @param <E>
     * @return a new empty dynamic Array.
     */
    public static <E> _A<E> _a() {
        return new _A<E>();
    }

    /**
     * Consumes an Iterator, saving the output in a List.
     * Make sure the Iterator terminates.
     *
     * @param it
     * @param <T>
     * @return
     */
    public static <T> _A<T> _a(final Iterator<? extends T> it) {
        final _A<T> out = _a();
        for (final T t : in(it)) out.add(t);
        return out;
    }

    /**
     * equivalent to it.iterator(), but shorter.
     *
     * @param it
     * @param <T>
     * @return the Iterable's iterator.
     */
    public static <T> Iterator<T> _i(final Iterable<T> it) {
        return it.iterator();
    }

    /**
     * Double-ended queue fake literal. Naked null elements are not allowed,
     * But you could have a queue of Thru with null referents.
     * {@link ArrayDeque}s generally perform better than linked list
     * implementations; inserts and removes are fast at both ends.
     * This method will cause issues when creating an array with unchecked generic
     * elements--Use the single element method with the chain syntax instead.
     *
     * @param e - an Array of type E.
     *          Multiple arguments will be converted to an array automatically.
     * @return An ArrayDeque with the provided elements.
     */
    //@SafeVarargs
    public static <E> _Q<E> _q(final E... e) {
        final _Q<E> out = new _Q<E>(e.length);
        Collections.addAll(out, e);
        return out;
    }

    /**
     * Double-ended queue fake literal with a single starting element.
     *
     * @param e
     * @param <E>
     * @return
     */
    public static <E> _Q<E> _q(final E e) {
        return new _Q<E>().and(e);
    }

    /**
     * @param <E>
     * @return a new empty double-ended queue.
     */
    public static <E> _Q<E> _q() {
        return new _Q<E>();
    }

    /**
     * Mutable tuple fake literal. A Tuple is just a fixed-length {@code List}.
     * Tuple syntax {@code _t("a","b","c")} is much shorter than the usual
     * equivalent invocation <code>Arrays.asList(new String[]{"a","b","c"})
     * </code>
     * Most collections can be initialized with one of these list views.
     *
     * @param e - an Array of type E.
     *          Multiple arguments will be converted to an array automatically.
     * @return a {@code List} view of the array, using {@link
     * Arrays#asList}.
     * It's still an array, with the usual limitations.
     */
    //@SafeVarargs
    public static <E> List<E> _t(final E... e) {
        return Arrays.asList(e);
    }

    /**
     * An the same as _f(). A fixed-length list of length zero is immutable,
     * since there's nothing to mutate.
     * @param <E>
     * @return
     */
    public static <E> List<E> _t(){
        return Collections.emptyList();
    }

    /**
     * Frozen tuple fake literal, equivalent to freeze(_t(...))
     * using {@link BuiltIn#freeze(List)}.
     * A Tuple is a fixed-length {@code List}.
     * Tuple syntax {@code _t("a","b","c")} is much shorter than the usual
     * equivalent invocation <code>Arrays.asList(new String[]{"a","b","c"})
     * </code>
     * Most collections can be initialized with one of these list views.
     *
     * @param e an Array of type E.
     *          Multiple arguments will be converted to an array automatically.
     * @return a {@code List} view of the array, using
     * {@link Collections#unmodifiableList(List)} on
     * {@link Arrays#asList(E... a)}.
     * The returned list is itself immutable, but may contain references to
     * mutable elements.
     */
    //@SafeVarargs
    public static <E> List<E> _f(final E... e) {
        return Collections.unmodifiableList(Arrays.asList(e));
    }

    /**
     * Frozen singleton fake literal, equivalent to
     * {@link Collections#singletonList(Object)}
     *
     * @param e
     * @param <E>
     * @return an immutable singleton list.
     */
    public static <E> List<E> _f(final E e) {
        return Collections.singletonList(e);
    }

    /**
     * @return the empty frozen tuple.
     */
    public static <E> List<E> _f() {
        return Collections.emptyList();
    }

    /**
     * Set fake literal. Sets are unordered collections without duplicate
     * elements. This particular implementation uses {@link LinkedHashSet},
     * which does remember order, though this is not required for the basic
     * Set interface. This is slightly slower than a simple HashSet (though
     * still constant time) for inserts and removes, but has the advantage
     * of iteration rate based on the linked list--as fast as the actual
     * length, not the larger capacity of the partially empty hash table.
     *
     * @param e an Array of type E. Multiple arguments will be converted
     *          to an array automatically.
     * @return A LinkedHashSet with the provided elements.
     */
    //@SafeVarargs
    public static <E> LinkedHashSet<E> _s(final E... e) {
        return new LinkedHashSet<E>(_t(e));
    }

    /**
     * @param <E>
     * @return new empty LinkedHashSet
     */
    public static <E> LinkedHashSet<E> _s() {
        return new LinkedHashSet<E>();
    }

    /**
     * {@link HashMap} fake literal. <br />
     * The fake literal
     * {@code Map<String,Integer> foo =  _x("foo",1)._("bar",2)._("baz",3);
     * } <br /> is much shorter than <br />
     * {@code Map<String, Integer> foo = new HashMap<String, Integer>(); }
     * <br />
     * {@code foo.put("foo",1); } <br />
     * {@code foo.put("bar",2); } <br />
     * {@code foo.put("baz",3); } <br />
     *
     * @param key   of the first pair put in the map. Determines the map's
     *              key type
     * @param value of the first pair put in the map. Determines the map's
     *              value type.
     * @return A {@link _X} instance backed by a HashMap.
     */
    public static <K, V> _X<K, V, HashMap<K, V>> _x(final K key, final V value) {
        return _X.map(key, value);
    }

    /**
     * {@link EnumMap} fake literal. This has the same form as the hash
     * map version, but must have an Enum key. {@link EnumMap}s have
     * array-like performance characteristics.
     *
     * @param key   must be an Enum type.
     * @param value of the first pair put in the map. Determines the map's
     *              value type.
     * @return A _X instance backed by an EnumMap
     */
    public static <K extends Enum<K>, V> _X<K, V, EnumMap<K, V>> _x(final K key, final V value) {
        return _X.map(key, value);
    }

    /**
     * {@link Map} fake literal. Use this to apply the
     * chain-initialization syntax to any existing Map instance. Example:
     * {@code _x(new TreeMap<String,String>())._("foo","bar")._("too","bar")}
     *
     * @param m - an existing Map
     * @return a _X instance wrapping m
     */
    public static <K, V, S extends Map<K, V>> _X<K, V, S> _x(final S m) {
        return _X.map(m);
    }

    /**
     * @return empty HashMap
     */
    public static <K, V> _X<K, V, HashMap<K, V>> _x() {
        return new _X<K, V, HashMap<K, V>>(new HashMap<K, V>());
    }

    /**
     * Array fake literal. Shorter than the equivalent
     * <code>new E[]{...}</code>. The type can be inferred from the
     * first argument. items(...) only works on reference types. To avoid
     * auto-boxing of primitives, use
     * booleans(...),
     * chars(String),
     * ints(...),
     * longs(...),
     * floats(...), or
     * doubles(...) instead. The byte and short primitives effectively don't
     * have literals except when declaring arrays, so no bytes(...) or shorts(...)
     *
     * @param e Multiple arguments will be converted to an array
     *          automatically.
     * @return the arguments as an array.
     */
    public static <E> E[] items(final E... e) {
        // NB. varargs are NOT safe here, since the return is an array.
        return e;
    }

    /*
     * short[] and byte[] primitive arrays are not provided. int literals
     * as arguments are not implicitly cast to bytes or shorts, so varargs
     * with byte... or short... would look like this:
     * shorts((short)1,(short)2,(short)3)
     * bytes((byte)1,(byte)2,(byte)3)
     * Java's native array constants are easier to type than that!
     * The main advantage of byte or short arrays over ints is saving memory.
     * The only way to make a fake literal is to use an int... vararg and
     * cast each element to byte or short in a for loop. It's not worth it.
     */

    /**
     * Primitive array fake literal.
     *
     * @param a arguments will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<boolean[], Boolean> booleans(final boolean... a) {
        return Primitives.valueOf(a);
    }

    /**
     * Primitive array fake literal.
     *
     * @param a String will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<char[], Character> chars(final String a) {
        return Primitives.valueOf(a.toCharArray());
    }

    /**
     * Primitive array fake literal.
     *
     * @param a arguments will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<int[], Integer> ints(final int... a) {
        return Primitives.valueOf(a);
    }

    /**
     * Primitive array fake literal with implied long cast
     *
     * @param a arguments will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<long[], Long> longs(final long... a) {
        return Primitives.valueOf(a);
    }

    /**
     * Primitive array fake literal with implied float cast.
     *
     * @param a arguments will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<float[], Float> floats(final float... a) {
        return Primitives.valueOf(a);
    }

    /**
     * Primitive array fake literal with implied double cast.
     *
     * @param a arguments will be converted to an array automatically.
     * @return the arguments as an array.
     */
    public static Primitives<double[], Double> doubles(final double... a) {
        return Primitives.valueOf(a);
    }

    /**
     * Fully generic cons cell class for implementing arbitrary linked
     * cons-cell data structures, or constructs for short.
     * A cons cell is a pair of arbitrary elements (called head and tail),
     * which themselves may be cons cells.
     * <p/>
     * Constructs may contain mixed
     * types that are statically checked. The short name "_" is required for
     * concise generic syntax.
     * <p/>
     * The typical construct is a linked stack structure, though other
     * constructs, such as trees are also possible.
     * Cons stacks constructed with the push(E) method can be statically type checked.
     * These stacks can be defined in-line, and can replace small struct-like classes or be used as
     * multi-typed tuples. Cons stacks may be unpacked with chained pop(Out)
     * <p/>
     * Cons cells support immutability; both head and tail are final. Mutability is not
     * required for push and pop. Both elements are fully generic and can be made mutable
     * if they contain mutable types, such as Thru.
     *
     * @param <H> type of the head element
     * @param <T> type of the tail element
     * @author Matthew Odendahl
     */
    public static class _<H, T> {
        /**
         * Analogous to Lisp car field. In a linked stack, contains the element.
         */
        public final H head;
        /**
         * Analogous to Lisp cdr field. In a linked stack, contains the next
         * node. At the end of a linked stack, contains (Void)null.
         */
        public final T tail;

        //prevents cycles in toString(). Cycles are possible with mutable elements like Thru<_<?,?>>
        private boolean seen = false;


        _(final H head, final T tail) {
            this.head = head;
            this.tail = tail;
        }

        /**
         * Wraps a new cons cell with the specified "element" head around "this" tail
         *
         * @param element -
         *                the element to prepend to the linked stack
         * @return a cons cell with "element" as head and "this" as tail
         */
        public <E> _<E, _<H, T>> push(final E element) {
            return new _<E, _<H, T>>(element, this);
        }

        /**
         * Used to unpack stacks. Example:<br />
         * {@code Out<Integer> x,y; } <br />
         * {@code Out<Character> a; } <br />
         * {@code _(1,_(2,_('a',VOID))).pop(x=_()).pop(y=_()).pop(a=_());} <br />
         *
         * @param head an Out to put the head in
         * @return the tail
         */
        public T pop(final Out<? super H> head) {
            head._ = this.head;
            return tail;
        }

        /**
         * @return a new cell with the same elements, but in opposite order.
         * The head becomes the tail and the tail becomes the head.
         */
        public _<T, H> flip() {
            return new _<T, H>(tail, head);
        }

        @Override
        public synchronized String toString() {
            // breaks cycles
            if (seen) return String.format("_(@%s...)", Integer.toHexString(super.hashCode()));
            seen = true;
            final String out = String.format("_(%s,%s)", str(head), str(tail));
            seen = false;
            return out;
        }

        /**
         * Comparing constructs with cycles is not recommended. It's still guaranteed to be
         * consistent, even with hashCode(), but the results are not always intuitive.
         * The equality check is recursive, so there is a (small) risk of stack overflow.
         */
        @Override
        public synchronized boolean equals(final Object o) {
            if (o == null) return false;//"this" is never null
            if (o == this) return true;//reflexive
            if (o instanceof _<?, ?>) {
                // cycles could produce different hash codes with technically the same structure
                if (hashCode() != o.hashCode()) return false;//consistent with hashCode()
                // with such cycles, hash codes might just so happen to be the same
                if (toString().equals(o.toString())) return false;
                // now the chances of cycles not caught by the reflexive check are remote
                // the worst that could happen is a stack overflow
                final _<?, ?> cell = (_<?, ?>) o;
                return (eq(head, cell.head) && eq(tail, cell.tail));
            }
            return false;
        }

        /**
         * Using mutable data structures as hash keys is ill-advised. Changing the contents changes
         * equality, so the hash code must also change. This means it must be re-calculated for
         * every call, which may adversely impact the performance of the hash table.
         */
        @Override
        public synchronized int hashCode() {
            if (seen) return super.hashCode(); // breaks cycles
            int out = -1;
            seen = true;
            out = 31 * out + (head.hashCode());
            out = 31 * out + (tail.hashCode());
            seen = false;
            return out;
        }


        private static final Cdr<?> CDR = new Cdr<Object>();

        /**
         * @return an instance of the Cdr Function,
         * which returns the tail when applied
         */
        // an instance of class Car has no state; types are interchangeable.
        @SuppressWarnings("unchecked")
        public static <T> Cdr<T> cdr() {
            return (Cdr<T>) CDR;
        }

        private static final Car<?> CAR = new Car<Object>();

        /**
         * @return an instance of the Car Function,
         * which returns the head when applied to a cons cell
         */
        // an instance of class Car has no state; types are interchangeable.
        @SuppressWarnings("unchecked")
        public static <H> Car<H> car() {
            return (Car<H>) CAR;
        }

        /**
         * Function subclass that returns the head of a cell when applied.
         *
         * @param <H>
         * @author mattheweganodendahl
         */
        public static class Car<H> extends Function<_<? extends H, ?>, H> {

            @Override
            public H apply(final _<? extends H, ?> cell) {
                return cell.head;
            }

            private Car() {
            }

        }

        /**
         * Function sublcass that returns the tail of a cell when applied.
         *
         * @param <T>
         * @author mattheweganodendahl
         */
        public static class Cdr<T> extends Function<_<?, ? extends T>, T> {

            @Override
            public T apply(final _<?, ? extends T> cell) {
                return cell.tail;
            }

            private Cdr() {
            }
        }

    }

    /**
     * A mutable reference. Used to get output through parameters.
     * Out starts with a null referent. Methods with Out parameters should
     * expect them to contain null referents and will replace them. Out should
     * never be used for input, use Thru instead.
     *
     * @param <T> referent type
     * @author Matthew Odendahl
     */
    public static class Out<T> {
        /**
         * The referent for this Out
         */
        public T _;

        Out() {
        }

        @Override
        public String toString() {
            return String.format("_(%s)", str(_));
        }

        @Override
        public int hashCode() {
            return hash(_);
        }

        @Override
        public boolean equals(final Object o) {
            return o != null && o instanceof Out && eq(_, ((Out) o)._);
        }
    }

    /**
     * A mutable reference. Used to get input and output through
     * parameters. The static generator allows for non-null initialization,
     * this is otherwise identical to Out. Methods may use Thru instead of Out
     * to indicate they expect input referents.
     *
     * @param <T> referent type
     * @author Matthew Odendahl
     */
    public static class Thru<T> extends Out<T> {
        private Thru(final T t) {
            _ = t;
        }
    }

    /**
     * extends ArrayList with fake literal chain syntax:
     * ._(e) and ._(a,b,c,d,e)
     *
     * @author Matthew Odendahl
     */
    public static class _A<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;

        /**
         * @see ArrayList#ArrayList(Collection)
         */
        public _A(final Collection<? extends E> c) {
            super(c);
        }

        /**
         * @see ArrayList#ArrayList(int)
         */
        public _A(final int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * @see ArrayList#ArrayList()
         */
        public _A() {
            super();
        }

        /**
         * Array fake literal chain. Adds another element to the list.
         * Java does not allow the creation of generic arrays. The usual idiom
         * to quickly initialize an ArrayList by creating an array constant
         * and passing it as a list to the constructor fails when attempting
         * to create a generic array. Chained calls are an alternative that
         * work even with generic elements.
         * Hint: copy and paste ")._(" when typing chains.
         *
         * @param element - the element to append
         * @return this
         */
        public _A<E> _(final E element) {
            this.add(element);
            return this;
        }

        /**
         * Array fake literal chain. Adds five elements to the list.
         * Equivalent to _(a)._(b)._(c)._(d)._(e), but _(a,b,c,d,e) is more
         * compact. Grouping by fives also helps keep track of indexes when
         * creating large lists.
         *
         * @param a
         * @param b
         * @param c
         * @param d
         * @param e
         * @return this
         * @see _A#_(Object)
         */
        public _A<E> _(
                final E a, final E b, final E c, final E d, final E e) {
            add(a);
            add(b);
            add(c);
            add(d);
            add(e);
            return this;
        }
    }

    /**
     * Extends ArrayDeque with chain syntax.
     * Chain pop with next(Out). Chain push with ahead(E).
     * Chain append with and(E). Chain pop from the last with last(Out).
     *
     * @param <E>
     */
    public static class _Q<E> extends ArrayDeque<E> {
        public _Q() {
            super();
        }

        public _Q(final int numElements) {
            super(numElements);
        }

        public _Q(final Collection<? extends E> c) {
            super(c);
        }

        /**
         * pops Out the element at head and returns this deque (now the
         * remaining tail).
         *
         * @param e
         * @return
         */
        public _Q<E> next(final Out<? super E> e) {
            e._ = pop();//removeFirst();
            return this;
        }

        /**
         * pushes the element ahead of the deque and returns this deque
         * (now with a new head).
         *
         * @param e
         * @return
         */
        public _Q<E> ahead(final E e) {
            push(e);//addFirst
            return this;
        }

        /**
         * takes Out the last element and returns this deque (now the
         * remaining init).
         *
         * @param e
         * @return
         */
        public _Q<E> last(final Out<? super E> e) {
            e._ = removeLast();
            return this;
        }

        /**
         * appends element as the last element in the deque.
         *
         * @param e
         * @return
         */
        public _Q<E> and(final E e) {
            add(e);//addLast
            return this;
        }
    }

    /**
     * Map wrapper "_X" class. Adds the chain-put method {@code _(K,V)} to
     * any {@link Map} type. Unwrap the map by getting the public {@code map}
     * field.
     *
     * @param <K> key type of the backing map
     * @param <V> value type of the backing map
     * @param <T> type of the backing map. Additional methods not in the
     *            {@code Map<K,V>} interface will still be accessible in the
     *            {@code public final T} field {@code map}
     * @author Matthew Odendahl
     */
    public static class _X<K, V, T extends Map<K, V>> extends AbstractMap<K, V> {
        /**
         * The map backing the _X. _X implements the Map interface for
         * convenience, but the backing map may have better implementations for
         * some methods. Access to the backing map allows the use of any extra
         * methods not in the interface.
         */
        public final T _;

        private _X(final T m) {
            _ = m;
        }

        // implementations of map fake literals
        static <K, V, S extends Map<K, V>> _X<K, V, S> map(final S m) {
            return new _X<K, V, S>(m);
        }

        static <K extends Enum<K>, V> _X<K, V, EnumMap<K, V>>
        map(final K key, final V value) {
            return new _X<K, V, EnumMap<K, V>>
                    (new EnumMap<K, V>(key.getDeclaringClass()))._(key, value);
        }

        static <K, V> _X<K, V, HashMap<K, V>> map(
                final K key, final V value) {
            return new _X<K, V, HashMap<K, V>>
                    (new HashMap<K, V>())._(key, value);
        }

        /**
         * Map fake literal chain. Adds another pair to the map.
         * Hint: copy and paste ")._(" when building long chains.
         *
         * @param key
         * @param value
         * @return this
         */
        public _X<K, V, T> _(final K key, final V value) {
            _.put(key, value);
            return this;
        }

        /**
         * Puts all returned key/value pairs into the map.
         * Example usage: myMwrap.and(zip(_t('a','b','c'),_t(1,2,3)))
         * results in map('a',1)._('b',2)._('c',3)
         *
         * @param pairs - a list of _(key,value) cons pairs.
         * @return this
         */
        public _X<K, V, T> and(final Iterator<? extends _<? extends K, ? extends V>> pairs) {
            for (final _<? extends K, ? extends V> pair : in(pairs)) {
                _.put(pair.head, pair.tail);
            }
            return this;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return _.entrySet();
        }

        @Override
        public V put(final K key, final V value) {
            return _.put(key, value);
        }

        /**
         * return map.containsKey(key);
         */
        @Override
        public boolean containsKey(final Object key) {
            return _.containsKey(key);
        }

        /**
         * return map.containsValue(value);
         */
        @Override
        public boolean containsValue(final Object value) {
            return _.containsValue(value);
        }

        /**
         * return map.get(key);
         */
        @Override
        public V get(final Object key) {
            return _.get(key);
        }

        /**
         * return map.remove(key);
         */
        @Override
        public V remove(final Object key) {
            return _.remove(key);
        }

    }

    /**
     * The Primitives class wraps an array of a primitive type in an object. An object of type
     * Primitives contains a public field which is the primitive array. Primitives also implements
     * the List interface for the equivalent boxed type, which can effectively make a primitive
     * array generic. However, using the List methods has an overhead due to boxing of primitives,
     * so is generally preferable to directly access the array.
     * It is possible to make an array of boxes, but arrays of primitives take less memory.
     * There will be considerable overhead in converting an array of primitives to an array
     * of objects (because they must be boxed element-by element), but wrapping an entire primitive
     * array in the Primitives class is trivial. The valueOf methods store the reference to the
     * array; the array is not copied; changes write through.
     *
     * @param <A>
     * @param <E>
     */
    public static final class Primitives<A, E> extends AbstractList<E> {
        public final A _;
        public final int length;

        private final Strategy<A, E> strategy;

        private static abstract class Strategy<A, E> {
            abstract E get(A a, int i);

            abstract E set(A a, int i, E e);
        }

        private static final Strategy<byte[], Byte> BYTES = new Strategy<byte[], Byte>() {
            @Override
            Byte get(final byte[] bytes, final int i) {
                return bytes[i];
            }

            @Override
            Byte set(final byte[] bytes, final int i, final Byte element) {
                try {
                    return bytes[i];
                } finally {
                    bytes[i] = element;
                }
            }
        };

        private static final Strategy<short[], Short> SHORTS = new Strategy<short[], Short>() {
            @Override
            Short get(final short[] shorts, final int i) {
                return shorts[i];
            }

            @Override
            Short set(final short[] shorts, final int i, final Short element) {
                try {
                    return shorts[i];
                } finally {
                    shorts[i] = element;
                }
            }
        };

        private static final Strategy<int[], Integer> INTS = new Strategy<int[], Integer>() {
            @Override
            Integer get(final int[] ints, final int i) {
                return ints[i];
            }

            @Override
            Integer set(final int[] ints, final int i, final Integer element) {
                try {
                    return ints[i];
                } finally {
                    ints[i] = element;
                }
            }
        };

        private static final Strategy<long[], Long> LONGS = new Strategy<long[], Long>() {
            @Override
            Long get(final long[] longs, final int i) {
                return longs[i];
            }

            @Override
            Long set(final long[] longs, final int i, final Long element) {
                try {
                    return longs[i];
                } finally {
                    longs[i] = element;
                }
            }
        };

        private static final Strategy<float[], Float> FLOATS = new Strategy<float[], Float>() {
            @Override
            Float get(final float[] floats, final int i) {
                return floats[i];
            }

            @Override
            Float set(final float[] floats, final int i, final Float element) {
                try {
                    return floats[i];
                } finally {
                    floats[i] = element;
                }
            }
        };

        private static final Strategy<double[], Double> DOUBLES = new Strategy<double[], Double>() {
            @Override
            Double get(final double[] doubles, final int i) {
                return doubles[i];
            }

            @Override
            Double set(final double[] doubles, final int i, final Double element) {
                try {
                    return doubles[i];
                } finally {
                    doubles[i] = element;
                }
            }
        };

        private static final Strategy<char[], Character> CHARS = new Strategy<char[], Character>() {
            @Override
            Character get(final char[] chars, final int i) {
                return chars[i];
            }

            @Override
            Character set(final char[] chars, final int i, final Character element) {
                try {
                    return chars[i];
                } finally {
                    chars[i] = element;
                }
            }
        };

        private static final Strategy<boolean[], Boolean> BOOLEANS =
                new Strategy<boolean[], Boolean>() {
                    @Override
                    Boolean get(final boolean[] booleans, final int i) {
                        return booleans[i];
                    }

                    @Override
                    Boolean set(final boolean[] booleans, final int i, final Boolean element) {
                        try {
                            return booleans[i];
                        } finally {
                            booleans[i] = element;
                        }
                    }
                };

        // Constructor is private. Use the static methods to wrap an array.
        private Primitives(final A array, final Strategy<A, E> s) {
            length = Array.getLength(array);
            _ = array;
            this.strategy = s;
        }

        // Static overloads are for each primitive, since there's no generic
        // way to only accept arrays of primitives.

        public static Primitives<byte[], Byte> valueOf(final byte[] array) {
            return new Primitives<byte[], Byte>(array, BYTES);
        }

        public static Primitives<short[], Short> valueOf(final short[] array) {
            return new Primitives<short[], Short>(array, SHORTS);
        }

        public static Primitives<int[], Integer> valueOf(final int[] array) {
            return new Primitives<int[], Integer>(array, INTS);
        }

        public static Primitives<long[], Long> valueOf(final long[] array) {
            return new Primitives<long[], Long>(array, LONGS);
        }

        public static Primitives<float[], Float> valueOf(final float[] array) {
            return new Primitives<float[], Float>(array, FLOATS);
        }

        public static Primitives<double[], Double> valueOf(final double[] array) {
            return new Primitives<double[], Double>(array, DOUBLES);
        }

        public static Primitives<char[], Character> valueOf(final char[] array) {
            return new Primitives<char[], Character>(array, CHARS);
        }

        public static Primitives<boolean[], Boolean> valueOf(final boolean[] array) {
            return new Primitives<boolean[], Boolean>(array, BOOLEANS);
        }

        // AbstractList methods

        @Override
        public E get(final int i) {
            return strategy.get(_, i);
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public E set(final int i, final E e) {
            return strategy.set(_, i, e);
        }
    }
}
