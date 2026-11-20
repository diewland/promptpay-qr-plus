package com.diewland.android.qr_pp_plus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_ACC_ID = "STATE_ACC_ID";
    private static final String STATE_AMOUNT = "STATE_AMOUNT";
    private static final String STATE_REMARK = "STATE_REMARK";

    private SharedPreferences sharedPref;

    private EditText tv_acc_id;
    private EditText tv_amount;
    private EditText tv_remark;
    private MaterialButton btn_share;
    private TextInputLayout layout_acc_id;
    private ImageView img_qr;
    private TextView tv_qr_placeholder;

    // QR without remark, cached so typing a remark doesn't re-encode the QR
    private String qrPayload;
    private Bitmap qrBase;
    // what is shown and shared (qrBase plus remark, if any)
    private Bitmap qrBMP;

    private final ActivityResultLauncher<Intent> pickAmount = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    tv_amount.setText(data.getStringExtra(CalcActivity.EXTRA_AMOUNT));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeHelper.apply(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_acc_id = findViewById(R.id.account_id);
        tv_amount = findViewById(R.id.amount);
        tv_remark = findViewById(R.id.remark);
        btn_share = findViewById(R.id.share);
        layout_acc_id = findViewById(R.id.account_id_layout);
        img_qr = findViewById(R.id.qr);
        tv_qr_placeholder = findViewById(R.id.qr_placeholder);

        // restore input states (before attaching watchers, so the QR renders once)
        sharedPref = getSharedPreferences("SAVE_STATE", MODE_PRIVATE);
        tv_acc_id.setText(sharedPref.getString(STATE_ACC_ID, ""));
        tv_amount.setText(sharedPref.getString(STATE_AMOUNT, ""));
        tv_remark.setText(sharedPref.getString(STATE_REMARK, ""));

        // render qr-code on text-changed
        TextWatcher rerender = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { renderQR(); }
        };
        tv_acc_id.addTextChangedListener(rerender);
        tv_amount.addTextChangedListener(rerender);
        tv_remark.addTextChangedListener(rerender);
        renderQR();

        TextInputLayout layout_amount = findViewById(R.id.amount_layout);
        layout_amount.setEndIconOnClickListener(v -> pickAmount.launch(
                new Intent(this, CalcActivity.class)
                        .putExtra(CalcActivity.EXTRA_AMOUNT, tv_amount.getText().toString())));

        btn_share.setOnClickListener(v -> shareQR());
    }

    private void renderQR(){
        String acc_id = tv_acc_id.getText().toString();
        PromptPay.AccountType type = PromptPay.AccountType.of(acc_id);
        layout_acc_id.setHelperText(getString(helperTextFor(type)));

        String payload = PromptPay.payload(acc_id, tv_amount.getText().toString());
        if(payload == null){
            img_qr.setImageDrawable(null);
            tv_qr_placeholder.setVisibility(View.VISIBLE);
            btn_share.setEnabled(false);
            return;
        }

        if(!payload.equals(qrPayload)){
            qrPayload = payload;
            qrBase = QRCode.from(payload)
                    .to(ImageType.PNG)
                    .withSize(512, 512)
                    .bitmap();
        }

        String remark = tv_remark.getText().toString();
        qrBMP = remark.isEmpty() ? qrBase : drawTextToBitmap(qrBase, remark);

        img_qr.setImageBitmap(qrBMP);
        tv_qr_placeholder.setVisibility(View.GONE);
        btn_share.setEnabled(true);
    }

    private static int helperTextFor(PromptPay.AccountType type){
        if(type == null) return R.string.helper_account;
        switch (type) {
            case PHONE:       return R.string.account_type_phone;
            case NATIONAL_ID: return R.string.account_type_card;
            default:          return R.string.account_type_ewallet;
        }
    }

    // share the PNG through FileProvider, so no storage permission is needed
    // https://stackoverflow.com/a/30172792/466693
    private void shareQR(){
        File dir = new File(getCacheDir(), "images");
        dir.mkdirs();
        // drop images from earlier shares
        File[] old = dir.listFiles();
        if(old != null){
            for(File f : old) f.delete();
        }

        File file = new File(dir, "pp-" + UUID.randomUUID() + ".png");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            qrBMP.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        new ShareCompat.IntentBuilder(this)
                .setType("image/png")
                .setStream(contentUri)
                .setChooserTitle("Choose an app")
                .startChooser();
    }

    // draw remark text centered in the QR's top quiet zone
    private static Bitmap drawTextToBitmap(Bitmap bitmap, String text) {
        Bitmap.Config config = bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
        // QRGen bitmaps are immutable, so draw on a mutable copy
        bitmap = bitmap.copy(config, true);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(61, 61, 61)); // #3D3D3D
        paint.setTextAlign(Paint.Align.CENTER);

        // size relative to the bitmap (not screen density) so shared images look
        // the same on every device; shrink long remarks to fit the width
        float maxWidth = bitmap.getWidth() * 0.9f;
        paint.setTextSize(bitmap.getWidth() * 0.06f);
        float textWidth = paint.measureText(text);
        if(textWidth > maxWidth){
            paint.setTextSize(paint.getTextSize() * maxWidth / textWidth);
        }

        new Canvas(bitmap).drawText(text, bitmap.getWidth() / 2f, bitmap.getHeight() * 0.09f, paint);
        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPref.edit()
                .putString(STATE_ACC_ID, tv_acc_id.getText().toString())
                .putString(STATE_AMOUNT, tv_amount.getText().toString())
                .putString(STATE_REMARK, tv_remark.getText().toString())
                .apply();
    }
}
