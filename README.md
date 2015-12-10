# Saccharin
Fake syntactic sugar with a metallic aftertaste! Because sometimes you have to have Java, but want something sweeter.

Saccharin is a pure Java library that improves upon Java's tediously verbose syntax through clever use of static imports,
anonymous inner classes, chained method calls, and the like. Saccharin will make your code shorter. 
It requires Java 6 or later.

Many of these utilities are based on ideas from more expressive languages like Python, Haskell, Lisp and even Java 8.

## Fake Literal Syntax
Strings have a simple literal syntax in Java: `"Hello, World!"`. It's simple and easy to use. Just imagine if you had to write 
```Java
new char[] {'H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!'}
```
every time you needed a String? Crazy right? 

Strings are an exception, but Java makes you put in that level of effort for other common data structures, like mappings:
```Java
Map<Integer, String> foo = new HashMap<Integer, String>();
foo.put(1, "one");
foo.put(2, "two");
foo.put(3, "three");
```
But in Python it's a breeze.
```Python
foo = {1:'one', 2: 'two', 3: 'three'}
```
Why can't it be this easy in Java?

In Saccharin it can be!
```
Map<Integer, String> foo = _X(1, "one")._(2, "two")._(3, "three");
```

## Sequence Operations
### Lazy Sequences
## Functional Style

filter/map/reduce
