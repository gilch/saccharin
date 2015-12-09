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

package com.github.gilch.saccharin.sequential;

import java.util.Iterator;

/**
 * Itr is both an Iterable and (via delegation) the last Iterator returned by Itr.
 * With Iter, it becomes possible to use the new style foreach syntax while retaining
 * access to the iterator.
 *
 * @param <E>
 */
public class Itr<E> implements Iterable<E>, Iterator<E> {

    private final Iterable<E> it;
    private Iterator<E> last;

    public Itr(final Iterable<E> it) {
        this.it = it;
    }

    @Override
    public Iterator<E> iterator() {
        last = this.it.iterator();
        return last;
    }

    @Override
    public boolean hasNext() {
        return last.hasNext();
    }

    @Override
    public E next() {
        return last.next();
    }

    @Override
    public void remove() {
        last.remove();
    }
}
