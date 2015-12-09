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

import java.util.NoSuchElementException;

/**
 * This is an abstract sequence adapter. It provides a skeletal implementation of Iterator to
 * minimize the effort required to implement this interface.
 * <p/>
 * The programmer need only extend this class with an implementation of getNext(). The getNext()
 * method must throw a NoSuchElementException if there isn't a next.
 * <p/>
 * The remove() method throws UnsupportedOperationException.
 * The hasNext() and next() methods are implemented based on the abstract getNext(). This class
 * will attempt to pre-fetch the next element to determine if it hasNext().
 *
 * @param <E>
 */
public abstract class LookAheadSequence<E> extends SequenceAdapter<E> {
    private static abstract class Strategy {
        abstract boolean onHasNext(LookAheadSequence l);

        abstract void onNext(LookAheadSequence l);
    }

    // simple state machine via subclasses.
    private static final Strategy DONE = new Strategy() {
        @Override
        boolean onHasNext(final LookAheadSequence l) {
            return false;
        }

        @Override
        void onNext(final LookAheadSequence l) {
            throw new NoSuchElementException();
        }
    };
    private static final Strategy FRESH = new Strategy() {
        @Override
        boolean onHasNext(final LookAheadSequence l) {
            return true;
        }

        @Override
        void onNext(final LookAheadSequence l) {
            l.state = USED;
        }
    };
    private static final Strategy USED = new Strategy() {
        @Override
        boolean onHasNext(final LookAheadSequence l) {
            return l.refresh().onHasNext(l);
        }

        @Override
        void onNext(final LookAheadSequence l) {
            l.refresh().onNext(l);
        }
    };

    private Strategy state = USED;//starting state.
    private E ahead;//the look-ahead element.

    private Strategy refresh() { // looks ahead.
        try {
            ahead = getNext();
            return state = FRESH;
        } catch (final NoSuchElementException e) {
            return state = DONE;
        }
    }

    @Override
    public final boolean hasNext() {
        return state.onHasNext(this);
    }

    @Override
    public final E next() {
        state.onNext(this);
        return ahead;
    }

    protected abstract E getNext() throws NoSuchElementException;
}
