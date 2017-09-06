package com.diewland.android.qr_pp_plus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

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

        Button share_btn = (Button)findViewById(R.id.share);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/png");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                qrBMP.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), qrBMP, "Title", null);
                Uri imageUri =  Uri.parse(path);
                share.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(Intent.createChooser(share, "Select"));
            }
        });
    }

    private void renderQR(){
        String pp_acc_id = "";
        String pp_amount = "";
        String pp_chksum = "";

        // process acc_id
        String acc_id = tv_acc_id.getText().toString();
        if(acc_id.length() == 13){ // card-id
            pp_acc_id = "0213" + acc_id;
            l("ID_CARD", pp_acc_id );
        }
        else if((acc_id.length() == 10) && (acc_id.startsWith("08"))){ // tel-no
            pp_acc_id = "01130066" + acc_id.substring(1);
            l("TEL_NO", pp_acc_id );
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
            l("AMOUNT", pp_amount );
        }

        // build pp string
        String pp_str = "00020101021229370016A000000677010111"
                      + pp_acc_id
                      + "5303764"
                      + pp_amount
                      + "5802TH"
                      + "6304";

        // process checksum
        pp_chksum = CRC16.checksum(pp_str);
        pp_str += pp_chksum;

        l("CHECKSUM", pp_chksum);
        l("PP_STR", pp_str);

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

    private void l(String type, String msg){
        // Log.d("DIEWLAND", "<"+ type +"> "+ msg);
    }
}
