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
 * This class is abstract. The remove() method always throws
 * UnsupportedOperationException. The hasNext() method always returns
 * true, so it must be interrupted externally.
 * <p/>
 * Useful for defining an on-the-fly sequence made with anonymous inner class
 * Iterators that don't support remove() and don't self-terminate.
 * Inheriting from this class instead of directly implementing Iterator
 * saves the trouble of defining the remove() and hasNext() methods.
 *
 * @param <E> element type of the iterator
 * @author Matthew Odendahl
 * @see SequenceAdapter
 * @see Iterator
 */
public abstract class InfiniteSequence<E> extends SequenceAdapter<E> {
    @Override
    public final boolean hasNext() {
        return true;
    }
}
