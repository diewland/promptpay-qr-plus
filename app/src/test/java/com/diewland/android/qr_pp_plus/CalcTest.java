package com.diewland.android.qr_pp_plus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalcTest {

    @Test
    public void formatsIntegerOneAndTwoDecimals() {
        assertEquals("300", Calc.ops(250.5, 49.5, "+"));
        assertEquals("2.5", Calc.ops(5, 2, "÷"));
        assertEquals("0.33", Calc.ops(1, 3, "÷"));
        assertEquals("-3", Calc.ops(2, 5, "-"));
        assertEquals("7.5", Calc.ops(2.5, 3, "x"));
    }

    @Test
    public void recognisesOperators() {
        assertTrue(Calc.is_math_ops("x"));
        assertTrue(Calc.is_math_ops("÷"));
        assertFalse(Calc.is_math_ops("="));
    }
}
