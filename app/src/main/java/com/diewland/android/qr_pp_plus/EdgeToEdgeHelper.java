package com.diewland.android.qr_pp_plus;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Targeting API 35+ forces edge-to-edge drawing and the system no longer paints
 * status/navigation bar colors. This keeps the original look: transparent bars
 * with light icons over a colorPrimaryDark window background, and the layout
 * root inset by margins so nothing is drawn under system bars, cutouts or the IME.
 *
 * Call after setContentView().
 */
final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {}

    static void apply(AppCompatActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));

        activity.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(activity, R.color.colorPrimaryDark)));

        ViewGroup content = activity.findViewById(android.R.id.content);
        final View root = content.getChildAt(0);
        root.setBackgroundColor(themeBackgroundColor(activity));

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.setMargins(insets.left, insets.top, insets.right, insets.bottom);
            v.setLayoutParams(lp);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private static int themeBackgroundColor(AppCompatActivity activity) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorBackground});
        try {
            return a.getColor(0, Color.WHITE);
        } finally {
            a.recycle();
        }
    }
}
