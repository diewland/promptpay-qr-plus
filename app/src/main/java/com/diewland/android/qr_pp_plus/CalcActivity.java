package com.diewland.android.qr_pp_plus;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class CalcActivity extends AppCompatActivity {

    /** Intent extra carrying the amount string, both into and out of this screen. */
    public static final String EXTRA_AMOUNT = "AMOUNT";

    // keys dispatched by their label text
    private static final int[] TEXT_KEYS = {
            R.id.pad_0, R.id.pad_1, R.id.pad_2, R.id.pad_3, R.id.pad_4,
            R.id.pad_5, R.id.pad_6, R.id.pad_7, R.id.pad_8, R.id.pad_9,
            R.id.pad_dot, R.id.pad_divide, R.id.pad_multiply, R.id.pad_minus, R.id.pad_add,
    };

    private EditText screen;
    private TextView mod;
    private MaterialButton pad_equal;
    private ColorStateList equalTint;
    private ColorStateList equalTextColor;

    // calculator state
    private Double calc_a    = null;
    private String calc_mod  = null;
    private String calc_prev = null;
    private boolean okMode   = false; // "=" key currently shows "OK"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        EdgeToEdgeHelper.apply(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        screen = findViewById(R.id.screen);
        // the on-screen keypad is the input; never pop up the system keyboard
        screen.setShowSoftInputOnFocus(false);
        mod = findViewById(R.id.mod);
        pad_equal = findViewById(R.id.pad_equal);
        equalTint = pad_equal.getBackgroundTintList();
        equalTextColor = pad_equal.getTextColors();

        View.OnClickListener textKey = v -> press(((Button) v).getText().toString());
        for(int id : TEXT_KEYS){
            findViewById(id).setOnClickListener(textKey);
        }
        findViewById(R.id.pad_del).setOnClickListener(v -> press("DEL"));
        pad_equal.setOnClickListener(v -> press(okMode ? "OK" : "="));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load amount from previous screen
        String amt = getIntent().getStringExtra(EXTRA_AMOUNT);
        if(amt != null){
            screen.setText(amt.isEmpty() ? "0" : amt);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setOkMode(boolean ok){
        okMode = ok;
        if(ok){
            pad_equal.setText("OK");
            pad_equal.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
            pad_equal.setTextColor(ContextCompat.getColor(this, R.color.md_on_tertiary_container));
        }
        else {
            pad_equal.setText("=");
            pad_equal.setBackgroundTintList(equalTint);
            pad_equal.setTextColor(equalTextColor);
        }
    }

    // apply the pending operator to calc_a and the number on screen
    private void compute(String scr){
        screen.setText(Calc.ops(calc_a, Double.parseDouble(scr), calc_mod));
        calc_a = null;
        setOkMode(true);
    }

    private void press(String v){
        String scr = screen.getText().toString();

        if(v.equals("DEL")){
            scr = scr.substring(0, scr.length()-1);
            if(scr.isEmpty()){
                scr = "0";
                calc_a = null;
                calc_mod = null;
            }
            screen.setText(scr);
            mod.setText("");
            setOkMode(false);
        }
        else if(Calc.is_math_ops(v)){ // +-x÷
            if(!scr.isEmpty()){
                if(calc_a != null){ // chained operator: do equal action first
                    compute(scr);
                }
                mod.setText(v);
                calc_mod = v;
            }
        }
        else if(v.equals("=")){
            if(calc_a != null && calc_mod != null && !scr.isEmpty()){
                compute(scr);
                calc_mod = null;
            }
        }
        else if(v.equals("OK")){
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_AMOUNT, scr));
            finish();
        }
        else { // 1234567890.
            setOkMode(false);

            // handle dot case
            if(v.equals(".")){
                if(scr.isEmpty() ||                          // 1. blank screen
                   (calc_mod != null && calc_a == null)){    // 2. assigned ops
                    v = "0.";
                }
            }

            // handle state & special cases
            if("=".equals(calc_prev)     // reset when press number after =
               || "0".equals(scr)){      // zero on screen
                screen.setText(v);
            }
            else if(calc_mod == null){   // typing calc_a
                screen.append(v);
            }
            else if(calc_a == null){     // first digit of calc_b
                calc_a = Double.parseDouble(scr);
                screen.setText(v);
                mod.setText("");
            }
            else {                       // typing calc_b
                screen.append(v);
            }
        }
        calc_prev = v;
    }
}
