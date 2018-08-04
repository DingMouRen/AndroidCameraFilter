package com.dingmouren.androidcamerafilter;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dingmouren.camerafilter.FilterCameraActivity;
import com.dingmouren.camerafilter.FilterImageActivity;
import com.dingmouren.camerafilter.util.ConvertBitmapUtils;


import org.wysaid.common.Common;
import org.wysaid.nativePort.CGENativeLibrary;

import java.io.IOException;
import java.io.InputStream;

import static com.dingmouren.camerafilter.FilterImageActivity.BITMAP_FILTER;
import static com.dingmouren.camerafilter.FilterImageActivity.REQUEST_CODE_FILTER_IMG;

public class MainActivity extends AppCompatActivity {
    private ImageView mImg_1,mImg_2;
    private Button mBtnImg;

    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initListener();
    }

    private void initView() {
        mImg_1 = findViewById(R.id.img_1);
        mImg_2 = findViewById(R.id.img_2);
        mBtnImg = findViewById(R.id.btn_img);

        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img_girl);
        mImg_1.setImageResource(R.drawable.img_girl);
    }

    private void initListener(){
        mBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterImageActivity.disposeBitmap(MainActivity.this, ConvertBitmapUtils.bitmapToByteArray(mBitmap));
            }
        });
    }

    public void cameraFilter(View view){
        startActivity(new Intent(MainActivity.this, FilterCameraActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_FILTER_IMG:
                    if (data != null){
                        byte[] bytesArray = data.getByteArrayExtra(BITMAP_FILTER);
                        if (null != bytesArray && bytesArray.length > 0) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytesArray, 0, bytesArray.length);
                            mImg_2.setImageBitmap(bitmap);
                        }
                    }
                    break;
            }
        }
    }
}
