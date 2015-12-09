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

/**
 * Rx stands for recipe or prescription. Rx methods are methods that return an instance of this
 * class, and by convention end in "Rx". Rx methods have named default arguments (called Terms)
 * which can be set in chained calls before terminating with go(), which executes the method
 * with the current recipe. An Rx object may be reusable, meaning go() can be called more than
 * once, possibly with modification to the Rx object between calls. Rx methods can dramatically
 * reduce the need for method overloads and setters. Inspired by Python keyword args.
 *
 * @param <R>
 * @param <THIS>
 */
public abstract class Rx<R, THIS extends Rx<R, THIS>> {

    protected final <T> Term<T> is(final T defaultArg) {
        return new Term<T>(defaultArg);
    }

    protected final <T> Term<T> isNull() {
        return new Term<T>(null);
    }

    /**
     * executes the
     *
     * @return
     */
    public abstract R go();

    /**
     * Rx subclasses must have <br />
     * <code>protected getThis(){return this;}</code>
     *
     * @return this
     */
    protected abstract THIS getThis();

    /**
     * represents a single optional argument
     *
     * @param <T>
     * @author mattheweganodendahl
     */
    public class Term<T> {
        /**
         * The value of this argument.
         */
        public T _;

        private Term(final T value) {
            this._ = value;
        }

        /**
         * change the default value for this argument.
         *
         * @param override set the named argument to this value.
         * @return the enclosing optional arguments object, for chaining.
         */
        public THIS is(final T override) {
            this._ = override;
            return getThis();
        }

    }

}
