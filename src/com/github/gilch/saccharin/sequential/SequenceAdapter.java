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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class is abstract. The remove() method always throws UnsupportedOperationException. A
 * sequence is often generated on the fly, in which case remove() makes no sense, but an Iterator
 * may still be useful. The remove() method is not required for the foreach syntax.
 * <p/>
 * Useful for defining generators made with anonymous inner class
 * iterators that don't support remove(). Inheriting from this class
 * instead of directly implementing Iterator saves the trouble of
 * defining an unsupported remove() method.
 *
 * @param <E> element type of the iterator
 * @author Matthew Odendahl
 * @see Iterator
 */
public abstract class SequenceAdapter<E> implements Iterator<E>, Enumeration<E> {
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMoreElements() {
        return hasNext();
    }

    @Override
    public E nextElement() {
        return next();
    }
}
