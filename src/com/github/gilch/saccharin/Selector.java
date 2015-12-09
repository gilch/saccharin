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

import com.github.gilch.saccharin.sequential.InfiniteSequence;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Weighted pseudorandom item selector. The probability an element is
 * selected is proportional to the ratio of the weight of the element in
 * question to the weight of all elements known to the Selector.
 *
 * @param <T>
 * @author Matthew Odendahl
 */
public class Selector<T> extends InfiniteSequence<T> {
    private final NavigableMap<Double, T> domain = new TreeMap<Double, T>();
    private double total = 0;
    private final Random random = new Random();

    /**
     * Construct a Selector from a map of elements to their weights
     *
     * @param domain - map of elements to select from to their weights
     */
    public Selector(final Map<? extends T, Double> domain) {
        if (domain.isEmpty()) throw new IllegalArgumentException("empty");
        for (final Map.Entry<? extends T, Double> s : domain.entrySet()) {
            buildMap(s.getKey(), s.getValue());
        }
    }

    /**
     * Construct a Selector with a list of elements paired with weights
     *
     * @param pairs - items paired with their weights
     */
    public Selector(final Iterable<? extends Literal._<? extends T, Double>> pairs) {
        for (final Literal._<? extends T, Double> s : pairs) {
            buildMap(s.head, s.tail);
        }
        if (domain.isEmpty()) throw new IllegalArgumentException("empty");
    }

    /**
     * Construct a Selector to select among elements of equal weight.
     *
     * @param items - to select from
     */
    public Selector(final T... items) {
        if (items.length == 0) throw new IllegalArgumentException("empty");
        for (final T e : items) {
            buildMap(e, 1);
        }
    }

    private void buildMap(final T item, final double weight) {
        if (weight > 0) {
            /*
             * The *cumulative* weights become the new lookup keys.
             * Note the items become the values, even though
             * the map constructor has items as keys.
             * Keys must be unique in a Map, so equal weights couldn't be
             * entered in the constructor without losing an element
             * if the pairs were reversed.
             */
            total += weight;
            this.domain.put(total, item);
        }
    }

    /**
     * @return next random selection Elements with higher weight are more
     * likely to be returned. The probability an element is selected
     * is proportional to the ratio of the weight of the element in question
     * to the weight of all elements known to the Selector.
     */
    public T next() {
        return domain.ceilingEntry(random.nextDouble() * total).getValue();
    }

    /**
     * @param seed
     * @return this
     * @see Random#setSeed(long)
     */
    public Selector<T> setSeed(final long seed) {
        random.setSeed(seed);
        return this;
    }

}
