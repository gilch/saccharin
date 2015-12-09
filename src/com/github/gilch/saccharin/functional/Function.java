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

package com.github.gilch.saccharin.functional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * General-purpose generic function interface with an input and output.
 * Similar to the java.util.function.Function interface from Java 8
 * <p/>
 * Functions can be composed into compound functions using the
 * compose(before) or andThen(after) methods.
 * <p/>
 * Functions also have the powerful forEach method.
 *
 * @param <T> Single input parameter type. If multiple parameters are
 *            needed, this can be a parameter class, collection, or
 *            construct. If no parameters are needed, use Callable
 *            instead.
 * @param <R> return type. Use Void rather than Object for a null return.
 * @see Callable
 * @see Runnable
 * @see Predicate
 */
public abstract class Function<T, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t - the function argument
     * @return the function result
     */
    public abstract R apply(T t);

    /**
     * Returns a composed function that first applies the before function to
     * its input, and then applies this function to the result.
     *
     * @param before -
     *               the function to apply before this function is applied
     * @return a composed function that first applies the before function
     * and then applies this function
     */
    public final <V> Function<V, R>
    compose(final Function<? super V, ? extends T> before) {
        return new Function<V, R>() {
            @Override
            public R apply(final V v) {
                return Function.this.apply(before.apply(v));
            }
        };
    }

    /**
     * Returns a composed function that first applies this function to its
     * input, and then applies the after function to the result.
     *
     * @param after -
     *              the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the after function
     */
    public final <V> Function<T, V>
    andThen(final Function<? super R, ? extends V> after) {
        return new Function<T, V>() {
            @Override
            public V apply(final T t) {
                return after.apply(Function.this.apply(t));
            }
        };
    }

    /**
     * applies this function for each T in the list. Like the common map
     * function in functional languages.
     *
     * @param iterable - a list of inputs to apply to
     * @return a list of each result of application, in the order listed
     */
    public final List<R> forEach(final Iterable<? extends T> iterable) {
        final ArrayList<R> out = new ArrayList<R>();
        for (final T t : iterable) out.add(apply(t));
        return out;
    }

    /**
     * Lazy version of forEach which transforms an iterator by applying this
     * function when next is called. This is safe to use on infinite iterators.
     *
     * @param iterator
     * @return a transformed view of the iterator.
     */
    public final Iterator<R> forEach(final Iterator<? extends T> iterator) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return apply(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

}
