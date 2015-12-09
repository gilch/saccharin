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

import java.util.concurrent.Callable;

/**
 * function subclass that implements the Callable and Runnable interfaces,
 * but does not accept arguments. Effects are Functions, and can be
 * composed like them.
 *
 * @param <R> return type for effect(), apply(), and call()
 *            Use Void rather than Object for a null return.
 * @author Matthew Odendahl
 */
public abstract class Effect<R> extends Function<Void, R> implements Callable<R>, Runnable {
    @Override
    public R call() {
        return effect();
    }

    @Override
    public void run() {
        effect();
    }

    @Override
    public R apply(final Void t) {
        return effect();
    }

    /**
     * function called by call(), run(), and apply().
     * apply(t) ignores its argument, run() discards effect()'s result,
     * and call returns effect()'s result.
     *
     * @return the effect result
     */
    public abstract R effect();
}
