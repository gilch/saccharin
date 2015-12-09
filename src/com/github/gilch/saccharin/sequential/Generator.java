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

import com.github.gilch.saccharin.Literal;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import static com.github.gilch.saccharin.Literal._;

/**
 * The Generator class implements Python-like yield syntax. Java does not support continuations.
 * The place in the Generator is saved on a new call stack in a new background thread.
 * <p/>
 * If the iterator is not exhausted, the generator thread will not terminate until the iterator
 * is garbage collected, because otherwise the iterator might need to continue at a later time.
 * However, this means the program may not terminate at the end of the main thread unless
 * there is an explicit call to System.exit(int).
 *
 * @param <E>
 */
public abstract class Generator<E> {
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    //queues can't hold nulls directly, but generators might return them, and
    // there also has to be a termination signal, hence the cons pair.
    private final SynchronousQueue<Literal._<E, Boolean>> q =
            new SynchronousQueue<Literal._<E, Boolean>>();

    protected final void yield(final E e) throws InterruptedException {
        q.put(_(e, false));//new short-lived objects! HotSpot gc doesn't care, but Dalvik might.
    }

    /**
     * Call the yield(E) method within the generate() method.
     *
     * @throws InterruptedException
     */
    protected abstract void generate() throws InterruptedException;

    private boolean started = false;

    public final Iterator<E> start() {
        if (started) {
            throw new IllegalStateException(
                    "start() may only be called once per Generator instance");
        }

        final Literal.Out<Thread> putThread = _();

        pool.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        synchronized (putThread) {
                            putThread._ = Thread.currentThread();
                        }
                        try {
                            // signal that putThread has started.
                            q.put(_((E) null, true));//start signal.
                            generate();
                            q.put(_((E) null, true));//done signal.
                        } catch (final InterruptedException e) {
                            // putThread could be aborted by finalizer, correct action is nop.
                        } finally {
                            synchronized (putThread) {
                                putThread._ = null;
                            }
                        }
                    }
                });
        try {
            // Don't create the iterator until putThread initializes.
            q.take();//blocks until "start signal".
        } catch (final InterruptedException e) {
            //if an interruption happened here, there is a bug!
            throw new RuntimeException();
        }
        started = true;
        // create and return the iterator.
        return new LookAheadSequence<E>() {
            @Override
            protected E getNext() throws NoSuchElementException {
                try {
                    final _<E, Boolean> shuttle = q.take();
                    if (shuttle.tail) throw new NoSuchElementException();//done signal.
                    return shuttle.head;
                } catch (final InterruptedException e) {
                    throw new NoSuchElementException();
                }
            }

            /*
            The Generator is visible from the background thread, so it won't get garbage
            collected as long as the background thread exists. A generator may never terminate
            or may not be exhausted, which would be a memory leak without this finalizer.

            This delegate iterator exists only in the foreground (take) thread so it can be
            collected, and on finalize will kill the background thread. The garbage collector
            is not guaranteed to run; if you use generators, call System.exit(int) at the end
            of the program to ensure all remaining blocked threads terminate.
             */
            @Override
            protected void finalize() throws Throwable {
                try {
                    try {
                        synchronized (putThread) {
                            putThread._.interrupt();
                        }
                    } catch (NullPointerException e) {
                        // may have been nullified if putThread already completed or aborted.
                        // correct action is nop.
                    } finally {
                        super.finalize();
                    }
                } catch (final Throwable t) {
                    // unexpected throw and I want to know, but don't interrupt finalizers!
                    t.printStackTrace();
                }
            }
        };
    }

}
