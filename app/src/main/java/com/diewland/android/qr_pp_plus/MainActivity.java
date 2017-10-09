package com.diewland.android.qr_pp_plus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private String STATE_ACC_ID = "STATE_ACC_ID";
    private String STATE_AMOUNT = "STATE_AMOUNT";
    private String STATE_REMARK = "STATE_REMARK";

    private TextView tv_acc_id;
    private TextView tv_amount;
    private TextView tv_remark;
    private Button btn_share;
    private ImageView img_qr;
    private Bitmap qrBMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get app objects
        tv_acc_id = (TextView)findViewById(R.id.account_id);
        tv_amount = (TextView)findViewById(R.id.amount);
        tv_remark = (TextView)findViewById(R.id.remark);
        btn_share = (Button)findViewById(R.id.share);
        img_qr    = (ImageView) findViewById(R.id.qr);

        // render qr-code on text-changed
        tv_acc_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        tv_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        tv_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // bind share button
        Button share_btn = (Button)findViewById(R.id.share);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // use file provider for share bitmap W/O additional permissions
                // https://stackoverflow.com/a/30172792/466693
                File cachePath = new File(getCacheDir(), "images");
                String random_name = "pp-" + UUID.randomUUID() + ".png";

                // save bitmap to cache directory
                try {
                    cachePath.mkdirs(); // don't forget to make the directory
                    FileOutputStream stream = new FileOutputStream(cachePath + "/" + random_name); // overwrites this image every time
                    qrBMP.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // share intent
                File newFile = new File(cachePath, random_name);
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.diewland.android.qr_pp_plus.fileprovider", newFile);
                if (contentUri != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                    shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                }
            }
        });

        // restore input states
        sharedPref = getSharedPreferences("SAVE_STATE", MODE_PRIVATE);
        String prev_acc_id = sharedPref.getString(STATE_ACC_ID, "");
        String prev_amount = sharedPref.getString(STATE_AMOUNT, "");
        String prev_remark = sharedPref.getString(STATE_REMARK, "");
        if(!prev_acc_id.isEmpty()) tv_acc_id.setText(prev_acc_id);
        if(!prev_amount.isEmpty()) tv_amount.setText(prev_amount);
        if(!prev_remark.isEmpty()) tv_remark.setText(prev_remark);
    }

    // render QR image
    private void renderQR(){
        String pp_acc_id = "";
        String pp_amount = "";
        String pp_chksum = "";

        // process acc_id
        String acc_id = tv_acc_id.getText().toString();
        if(acc_id.length() == 15){ // truemoney e-wallet
            pp_acc_id = "0315" + acc_id;
        }
        else if(acc_id.length() == 13){ // card-id
            pp_acc_id = "0213" + acc_id;
        }
        else if(acc_id.length() == 10){ // tel-no
            pp_acc_id = "01130066" + acc_id.substring(1);
        }
        else { // invalid acc_id
            img_qr.setImageDrawable(null);
            btn_share.setVisibility(View.INVISIBLE);
            return;
        }

        // process amount
        String amount = tv_amount.getText().toString();
        if(!amount.isEmpty()){
            pp_amount = String.format("54%02d%s", amount.length(), amount);
        }

        // build pp string
        String pp_str = "00020101021129370016A000000677010111"
                      + pp_acc_id
                      + "5303764"
                      + pp_amount
                      + "5802TH"
                      + "6304";

        // process checksum
        pp_chksum = CRC16.checksum(pp_str);
        pp_str += pp_chksum;

        // render qr bitmap
        qrBMP = QRCode.from(pp_str)
                .to(ImageType.PNG)
                .withSize(512, 512)
                .bitmap();

        // if remark, paint into bitmap
        String remark = tv_remark.getText().toString();
        if(!remark.isEmpty()){
            qrBMP = drawTextToBitmap(this, qrBMP, remark);
        }

        // render bitmap
        img_qr.setImageBitmap(qrBMP);
        btn_share.setVisibility(View.VISIBLE);
    }

    // draw text on bitmap
    // https://www.skoumal.net/en/android-how-draw-text-bitmap/
    public Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        // paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = 0; //(bitmap.getWidth() - bounds.width())/2;
        int y = 45; //(bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    // save input state when pause
    @Override
    protected void onPause() {
        super.onPause();

        // save input states
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(STATE_ACC_ID, tv_acc_id.getText().toString());
        editor.putString(STATE_AMOUNT, tv_amount.getText().toString());
        editor.putString(STATE_REMARK, tv_remark.getText().toString());
        editor.commit();
    }
}
