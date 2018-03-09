package com.diewland.android.qr_pp_plus;

import java.util.Arrays;
import java.util.List;

public class Calc {

    private static List<String> math_ops = Arrays.asList(
         "+",
        "-",
        "x",
        "÷"
    );

    public static boolean is_math_ops(String v){
        return math_ops.contains(v);
    }

    public static String ops(Double a, Double b, String ops){
        Double result = null;
        if(ops.equals("+")){
            result = a + b;
        }
        else if(ops.equals("-")){
            result = a - b;
        }
        else if(ops.equals("x")){
            result = a * b;
        }
        else if(ops.equals("÷")){
            result = a / b;
        }
        // return 1 or 2 digit float or numeric
        if(result == result.intValue()){
            return String.valueOf(result.intValue());
        }
        else if(result == Double.parseDouble(String.format("%.1f", result))){
            return String.format("%.1f", result);
        }
        else {
            return String.format("%.2f", result);
        }
    }
}
