# Saccharin
Saccharin is a pure Java library that improves upon Java's tediously verbose syntax through clever
use of static imports, anonymous inner classes, chained method calls, and the like. Saccharin will
make your code shorter. It requires Java 6 or later.

Many of these utilities are based on ideas from more expressive languages like Python, Haskell,
Lisp and even Java 8.

## Fake Literal Syntax
Strings have a simple literal syntax in Java: `"Hello, World!"`. It's simple and easy to use.
Just imagine if you had to write
```Java
new char[] {'H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!'}
```
every time you needed a String? Crazy right? Not only is it harder to use, it's harder to read.

Strings are an exception, but Java makes you put in that level of effort for other common data
structures, like mappings:
```Java
Map<String, Integer> fooNorf = new HashMap<String, Integer>();
fooNorf.put("one", 1);
fooNorf.put("two", 2);
fooNorf.put("three", 3);
```
But in Python it's a breeze.
```Python
fooNorf = {'one': 1, 'two': 2, 'three' 3}
```
Why can't it be that easy in Java?

With Saccharin it can be!
```
Map<Integer, String> fooNorf = _x("one", 1)._("two", 2)._("three", 3);
```
Tastes like syntactic sugar, but it's fake!
The above code is pure Java. No compiler hacks. No bytecode manipulation.
There have been no changes to the Java grammar whatsoever. It's a simple static import.

Other common data structures use a similar short prefix.
```Java
//List view of an array ([t]uple)
_t(1,2,3)
//[f]rozen tuple (an unmodifiable List)
_f(1,2,3)
//double-ended [q]ueue
_q(1,2,3)
//dynamic [a]rray
_a(1,2,3)
// [s]et
_s(1,2,3)
// binary literals for Java 6 (long, but you can cast.)
_0b("1010"+"0100")
```
As you may have guessed by now these are just static methods with short names.
The fake array literals have slightly longer names.
```Java
//array of reference types. The type is implied by the arguments.
items("foo","bar") // same as: new String[] {"foo", "bar"}
//also works on primitives, but they're auto-boxed.
items(1,2,3) // same as: new Integer[] {1,2,3}
//array of primitive types have their own methods.
booleans(true, false)
chars("abc")  // converts from a String.
ints(1,2,3)
longs(1,2,3)
floats(1,2,3)  // note the implicit casts.
doubles(1,2,3)
// these are actually List views for easy collections interop,
List<Character> foo = chars("abc");
// convert to array with the "._" suffix.
char[] foo = chars("abc")._;
```
Simulate pass by reference with `Out` and `Thru`. A `Thru` is-an `Out`, but with an initial value.
```Java
// Out literal: _()
Out<Double> res = _();
new Object() {
    void test(double a, double b, Out<Double> res) {
        res._ = a - b;
    }
}.test(3, 7, res);
assertEquals(res, _(-4.0));
// Thru literal: _(foo)
Thru<String> foo = _("foo");
Thru<String> bar = _("bar");
// dereference with the usual "._" suffix.
new Object() {
    void test(Thru<String> a, Thru<String> b) {
        String temp = a._ + b._;
        b._ += a._;
        a._ = temp;
    }
}.test(foo, bar);
assertEquals(foo._, "foobar");
// Thru's will delegate equality to their contents.
assertEquals(bar, _("barfoo"));
```
The cons cell uses the same prefix as `Out` and `Thru`, but with two arguments.
```Java
//cons cell. Pairs any two reference types. Even another cons cell.
_(1,"one")
//cons cell linked list. Arbitrary collections of mixed types with no casting!
_<Float, _<Double, _<Character, Void>>> foo = _(1f, _(2d, _('3', null)));
```



## Sequence Operations
### Lazy Sequences
## Functional Style

filter/map/reduce
