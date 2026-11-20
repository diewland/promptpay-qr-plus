package com.diewland.android.qr_pp_plus;

import java.util.Arrays;
import java.util.List;

public class Calc {

    private static final List<String> MATH_OPS = Arrays.asList("+", "-", "x", "÷");

    public static boolean is_math_ops(String v){
        return MATH_OPS.contains(v);
    }

    public static String ops(double a, double b, String ops){
        double result;
        switch (ops) {
            case "+": result = a + b; break;
            case "-": result = a - b; break;
            case "x": result = a * b; break;
            case "÷": result = a / b; break;
            default: throw new IllegalArgumentException("unknown operator: " + ops);
        }
        // return integer, 1-digit or 2-digit decimal string
        if(result == (int) result){
            return String.valueOf((int) result);
        }
        String oneDigit = String.format("%.1f", result);
        if(result == Double.parseDouble(oneDigit)){
            return oneDigit;
        }
        return String.format("%.2f", result);
    }
}
