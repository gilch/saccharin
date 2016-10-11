package com.github.gilch.saccharin;

import org.junit.Test;

import java.util.*;

import static com.github.gilch.saccharin.Literal.*;
import static org.junit.Assert.*;

/**
 * Created by gilch on 12/9/2015.
 */
@SuppressWarnings("UnusedAssignment") // Many of these tests are at compile time.
public class LiteralTest {

    @Test
    public void test_cons() throws Exception {
        // statically typed constructs
        _<Integer, _<Double, _<Character, Void>>> foo = _(1, _(2.0, _('c', null)));
        _<_<_<Void, Character>, Double>, Integer> bar = _(_(_(null, 'c'), 2.0), 1);
        _<_<Integer, Double>, _<Character, Float>> baz = _(_(1, 2.0), _('c', 3f));
    }

    @Test
    public void test_cons_car_nil() throws Exception {
        // compiler assumes null is a Void.
        _<Integer, Void> foo = _(1, null);
    }

    @Test
    public void test_cons_nil_cdr() throws Exception {
        // compiler assumes null is a Void.
        _<Void, Integer> foo = _(null, 1);
    }

    @Test
    public void test_cons_nil_nil() throws Exception {
        // compiler assumes null is a Void.
        _<Void, Void> foo = _(null, null);
    }

    @Test
    public void test_thru() throws Exception {
        // Thru's primary use is multiple input and return via parameters.
        Thru<String> foo = _("foo");
        Thru<String> bar = _("bar");
        // dereference with ._
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
    }

    @Test
    public void test_out() throws Exception {
        // Out is primarily used to return results via parameters.
        Out<Double> res = _();
        new Object() {
            void test(double a, double b, Out<Double> res) {
                res._ = a - b;
            }
        }.test(3, 7, res);
        assertEquals(res, _(-4.0));
    }

    @Test
    public void test_0b() throws Exception {
        long f0 = _0b("1111" + "0000");
        assertEquals(f0, 0xF0L);
        assertEquals(_0b("-111" + "1111" + "1110" + "1101" + "1100" + "1011" + "1010" + "1001"
                        + "1000" + "0111" + "0110" + "0101" + "0100" + "0011" + "0010" + "0001"),
                -0x7FEDCBA987654321L);
    }

    @Test
    public void test_a() throws Exception {
        // multiple
        _A<Integer> foo = _a(1, 2, 3);
    }

    @Test
    public void test_a1() throws Exception {
        // single
        _A<Integer> foo = _a(1);
        // note compiler warning in Java6: unchecked generic varargs.
        _A<Thru<Integer>> bar = _a(_(1),_(2),_(3));
        // note lack of warning with chain from single. It's the same data.
        // this is the reason for a single argument overload.
        bar = _a(_(1))._(_(2))._(_(3));
        // @SafeVarargs does not exist in Java 6
    }

    @Test
    public void test_a2() throws Exception {
        // none
        _A<Object> foo = _a();
        _A<Integer> bar = _a();
        // note lack of unchecked generic varargs warning.
        // this is the reason for a zero argument overload.
        _A<Thru<Integer>> baz = _a();
    }

    @Test
    public void test_a3() throws Exception {
        // from iterator
        _A<Integer> foo = _a(_i(_t(1, 2, 3)));
    }

    @Test
    public void test_i() throws Exception {
        Iterator<Integer> foo = _i(_t(1, 2, 3));
    }

    @Test
    public void test_q() throws Exception {
        _Q<Integer> foo = _q(1, 2, 3);
    }

    @Test
    public void test_q1() throws Exception {
        _Q<Integer> foo = _q(1);
        // again no warning
        _Q<Thru<Integer>> bar = _q(_(1));
        bar = _q(_(1)).and(_(2)).and(_(3));
        bar = _q(_(3)).ahead(_(2)).ahead(_(1));
    }

    @Test
    public void test_q2() throws Exception {
        _Q<Object> foo = _q();
        _Q<Integer> bar = _q();
        // no warning
        _Q<Thru<Integer>> baz = _q();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void test_t() throws Exception {
        List<Integer> foo = _t(1, 2, 3);
        foo.add(0);
    }

    @Test
    public void test_t1() throws Exception {
        // _t() is the same as _f()
        List<Object> foo = _t();
        // which is the immutable empty list.
        assertTrue(foo == Collections.EMPTY_LIST);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void test_f() throws Exception {
        List<Integer> foo = _f(1, 2, 3);
        foo.set(0,0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void test_f1() throws Exception {
        List<Integer> foo = _f(1);
        foo.set(0,2);
    }

    @Test
    public void test_f2() throws Exception {
        // _t() is the same as _f()
        List<Object> foo = _f();
        // which is the immutable empty list.
        assertTrue(foo == Collections.EMPTY_LIST);
    }

    @Test
    public void test_s() throws Exception {
        LinkedHashSet<Integer> foo = _s(1, 2, 3);
    }

    @Test
    public void test_s2() throws Exception {
        LinkedHashSet<Object> foo = _s();
        LinkedHashSet<Float> bar = _s();
        LinkedHashSet<Thru<Float>> baz = _s();
    }

    @Test
    public void test_x() throws Exception {
        _X<String, Integer, HashMap<String, Integer>> foo2 = _x("foo", 2);
        // the usual suffix gets the underlying map
        HashMap<String, Integer> bar = _x("foo", 2)._;
    }

    enum Week {SU,MO,TU,WE,TH,FR,SA}
    @Test
    public void test_x1() throws Exception {
        // _x defaults to the more efficient EnumMap backing when possible.
        _X<Week, String, EnumMap<Week, String>> foo = _x(Week.MO, "Monday")._(Week.TU,"Tuesday");
    }

    @Test
    public void test_x2() throws Exception {
        _X<Object, Object, HashMap<Object, Object>> foo = _x();
        Map<String,String> bar = _x();
    }

    @Test
    public void test_x3() throws Exception {
        // it's possible to declare a different mapping type normally and extend with the chain
        _X<String, String, TreeMap<String, String>> footoo =
                _x(new TreeMap<String, String>())._("foo", "bar")._("too", "bar");
        TreeMap<String, String> footree = footoo._;
        // you can even use a preexisting map and wrap it again.
        _X<String, String, TreeMap<String, String>> fooyou = _x(footree)._("tree", "bar");
    }

    @Test
    public void testItems() throws Exception {
        String[] foo = items("foo", "bar", "baz");
    }

    @Test
    public void testBooleans() throws Exception {
        assertArrayEquals(new boolean[]{false,false,true}, booleans(false,false,true)._);
    }

    @Test
    public void testChars() throws Exception {
        assertArrayEquals(new char[]{'1','2','3',0}, chars("123\u0000")._);
    }

    @Test
    public void testInts() throws Exception {
        assertArrayEquals(new int[]{1,2,3},ints(1,2,3)._);
    }

    @Test
    public void testLongs() throws Exception {
        assertArrayEquals(new long[]{1,2,3},longs(1,2,3)._);
    }

    @Test
    public void testFloats() throws Exception {
        assertArrayEquals(new float[]{1,2,3},floats(1,2,3)._,0f);
    }

    @Test
    public void testDoubles() throws Exception {
        assertArrayEquals(new double[]{1,2,3},doubles(1,2,3)._,0.0);
    }
}
