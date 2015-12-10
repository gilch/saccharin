package com.github.gilch.saccharin;

import org.junit.Test;

import static com.github.gilch.saccharin.Literal.*;
import static org.junit.Assert.*;

/**
 * Created by gilch on 12/9/2015.
 */
public class LiteralTest {

    @Test
    public void test_cons() throws Exception {
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
        fail();
    }

    @Test
    public void test_a1() throws Exception {
        fail();

    }

    @Test
    public void test_a2() throws Exception {
        fail();

    }

    @Test
    public void test_a3() throws Exception {
        fail();

    }

    @Test
    public void test_i() throws Exception {
        fail();

    }

    @Test
    public void test_q() throws Exception {
        fail();

    }

    @Test
    public void test_q1() throws Exception {
        fail();

    }

    @Test
    public void test_q2() throws Exception {
        fail();

    }

    @Test
    public void test_t() throws Exception {
        fail();

    }

    @Test
    public void test_f() throws Exception {
        fail();

    }

    @Test
    public void test_f1() throws Exception {
        fail();

    }

    @Test
    public void test_f2() throws Exception {
        fail();

    }

    @Test
    public void test_s() throws Exception {
        fail();

    }

    @Test
    public void test_s1() throws Exception {
        fail();

    }

    @Test
    public void test_x() throws Exception {
        fail();

    }

    @Test
    public void test_x1() throws Exception {
        fail();

    }

    @Test
    public void test_x2() throws Exception {
        fail();

    }

    @Test
    public void test_x3() throws Exception {
        fail();

    }

    @Test
    public void testItems() throws Exception {
        fail();

    }

    @Test
    public void testBooleans() throws Exception {
        fail();

    }

    @Test
    public void testChars() throws Exception {
        fail();

    }

    @Test
    public void testInts() throws Exception {
        fail();

    }

    @Test
    public void testLongs() throws Exception {
        fail();

    }

    @Test
    public void testFloats() throws Exception {
        fail();

    }

    @Test
    public void testDoubles() throws Exception {
        fail();

    }
}