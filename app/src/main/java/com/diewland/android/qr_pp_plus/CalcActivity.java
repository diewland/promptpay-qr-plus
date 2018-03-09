package com.diewland.android.qr_pp_plus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class CalcActivity extends AppCompatActivity {

    String TAG = "DIEWLAND";

    EditText screen;
    TextView mod;
    Button pad_1;
    Button pad_2;
    Button pad_3;
    Button pad_4;
    Button pad_5;
    Button pad_6;
    Button pad_7;
    Button pad_8;
    Button pad_9;
    Button pad_0;
    Button pad_dot;
    Button pad_equal;
    Button pad_del;
    Button pad_divide;
    Button pad_multiply;
    Button pad_minus;
    Button pad_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        screen = (EditText)findViewById(R.id.screen);
        mod = (TextView) findViewById(R.id.mod);
        pad_1 = (Button)findViewById(R.id.pad_1);
        pad_2 = (Button)findViewById(R.id.pad_2);
        pad_3 = (Button)findViewById(R.id.pad_3);
        pad_4 = (Button)findViewById(R.id.pad_4);
        pad_5 = (Button)findViewById(R.id.pad_5);
        pad_6 = (Button)findViewById(R.id.pad_6);
        pad_7 = (Button)findViewById(R.id.pad_7);
        pad_8 = (Button)findViewById(R.id.pad_8);
        pad_9 = (Button)findViewById(R.id.pad_9);
        pad_0 = (Button)findViewById(R.id.pad_0);
        pad_dot = (Button)findViewById(R.id.pad_dot);
        pad_equal = (Button)findViewById(R.id.pad_equal);
        pad_del = (Button)findViewById(R.id.pad_del);
        pad_divide = (Button)findViewById(R.id.pad_divide);
        pad_multiply = (Button)findViewById(R.id.pad_multiply);
        pad_minus = (Button)findViewById(R.id.pad_minus);
        pad_add = (Button)findViewById(R.id.pad_add);

        List<Button> pads = Arrays.asList(
            pad_1,
            pad_2,
            pad_3,
            pad_4,
            pad_5,
            pad_6,
            pad_7,
            pad_8,
            pad_9,
            pad_0,
            pad_dot,
            pad_equal,
            pad_del,
            pad_divide,
            pad_multiply,
            pad_minus,
            pad_add
        );
        for(Button pad : pads){
            pad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                String scr = screen.getText().toString();
                String v = ((Button)view).getText().toString();
                handle_screen(scr, v);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load amount from previous screen
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String amt = extras.getString("AMOUNT");
            if(amt.length() == 0){ amt = "0"; }
            screen.setText(amt);
        }
    }

    // handle calculator logic
    Double calc_a    = null;
    Double calc_b    = null;
    String calc_mod  = null;
    String calc_prev = null;
    private void set_btn_equal(){
        pad_equal.setText("=");
        pad_equal.setTextColor(Color.BLACK);
    }
    private void set_btn_ok(){
        pad_equal.setText("OK");
        pad_equal.setTextColor(Color.BLUE);
    }
    private void handle_screen(String scr, String v){

        /*
        Log.d(TAG, String.format("press : %s", v));
        Log.d(TAG, String.format("screen : %s", scr));
        Log.d(TAG, String.format("calc_a : %s", calc_a));
        Log.d(TAG, String.format("calc_b : %s", calc_b));
        Log.d(TAG, String.format("calc_mod : %s", calc_mod));
        */

        if(v.equals("DEL")){ // delete
            scr = scr.substring(0, scr.length()-1);
            if(scr.length() == 0){
                scr = "0";
                calc_a = null;
                calc_b = null;
                calc_mod = null;
            }
            screen.setText(scr);
            mod.setText("");
            set_btn_equal();
        }
        else if(Calc.is_math_ops(v)){ // +-x÷
            if(scr.length() > 0){
                if(calc_a != null){ // do equal action
                    calc_b = Double.parseDouble(scr);
                    String result = Calc.ops(calc_a, calc_b, calc_mod);
                    screen.setText(result);
                    calc_a = null;
                    calc_b = null;
                    set_btn_ok();
                }
                mod.setText(v);
                calc_mod  = v;
            }
        }
        else if(v.equals("=")){ // equal
            if((calc_a != null)&&(calc_mod != null)&&(scr.length() > 0)){
                calc_b = Double.parseDouble(scr);
                String result = Calc.ops(calc_a, calc_b, calc_mod);
                screen.setText(result);
                calc_a = null;
                calc_b = null;
                calc_mod = null;
                set_btn_ok();
            }
        }
        else if(v.equals("OK")){
            Intent intent = new Intent();
            intent.putExtra("AMOUNT", screen.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
        else { // 1234567890.
           set_btn_equal();

            // handle dot case
            if(v.equals(".")){
               if((scr.length() == 0) ||                    // 1. blank screen
                 ((calc_mod != null) && (calc_a == null))){ // 2. assigned ops
                    v = "0.";
               }
            }

            // handle state & special cases
            if( ("=".equals(calc_prev))     // reset when press number after =
                || ("0".equals(scr))        // zero on screen
            ){
                screen.setText(v);
            }
            else if(calc_mod == null){      // calc_a
                screen.append(v);
            }
            else if(calc_mod != null){      // calc_b
                if(calc_a == null) {
                    calc_a = Double.parseDouble(scr);
                    screen.setText(v);
                    mod.setText("");
                }
                else {
                    screen.append(v);
                }
            }
        }
        calc_prev = v;
    }
}
