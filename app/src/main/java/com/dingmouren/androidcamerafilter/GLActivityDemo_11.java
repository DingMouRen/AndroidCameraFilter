package com.dingmouren.androidcamerafilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.dingmouren.camerafilter.camera.CameraV1Helper;
import com.dingmouren.camerafilter.camera.CameraV1;
import com.dingmouren.camerafilter.filter.FilterBase;
import com.dingmouren.camerafilter.filter.FilterBrightness;
import com.dingmouren.camerafilter.filter.FilterColorInvert;
import com.dingmouren.camerafilter.filter.FilterColorMatrix;
import com.dingmouren.camerafilter.filter.FilterContrast;
import com.dingmouren.camerafilter.filter.FilterGamma;
import com.dingmouren.camerafilter.filter.FilterGrayscale;
import com.dingmouren.camerafilter.filter.FilterHue;
import com.dingmouren.camerafilter.filter.FilterPixelation;
import com.dingmouren.camerafilter.CameraOperator;
import com.dingmouren.camerafilter.filter.FilterSepia;
import com.dingmouren.camerafilter.utils.Rotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.dingmouren.camerafilter.utils.Rotation.ROTATION_90;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class GLActivityDemo_11 extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private CameraOperator mCameraOperator;

    private CameraV1Helper mCameraV1Helper;
    private CameraV1 mCameraV1;

    private List<FilterBase> mFilters = new ArrayList<>();
    private int index = 0;
    private TextView mTvIndex;
    private ImageView imageView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_demo11);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mTvIndex = findViewById(R.id.tv_index);
        imageView = findViewById(R.id.img);


        mCameraOperator = new CameraOperator(this);
        mCameraOperator.setGLSurfaceView(mGLSurfaceView);

        mCameraV1Helper = new CameraV1Helper(this);
        mCameraV1 = new CameraV1(this, mCameraV1Helper, mCameraOperator);

        FilterBase filterBase = new FilterBase();
        FilterPixelation filterPixelation = new FilterPixelation();
        FilterColorInvert filterColorInvert = new FilterColorInvert();
        FilterContrast filterContrast = new FilterContrast();
        FilterHue filterHue = new FilterHue();
        FilterGamma filterGamma = new FilterGamma();
        FilterBrightness filterBrightness = new FilterBrightness();
        FilterSepia filterSepia = new FilterSepia();
        FilterGrayscale filterGrayscale = new FilterGrayscale();

        mFilters.add(filterBase);
        mFilters.add(filterPixelation);
        mFilters.add(filterColorInvert);
        mFilters.add(filterContrast);
        mFilters.add(filterHue);
        mFilters.add(filterGamma);
        mFilters.add(filterBrightness);
        mFilters.add(filterSepia);
        mFilters.add(filterGrayscale);

        mCameraOperator.setFilter(filterBase);

    }

    public void getPhoto(View view) {
        mCameraV1.takePicture("test", System.currentTimeMillis() + ".jpg");
    }


    public void changeFilter(View view) {
        mCameraOperator.setFilter(mFilters.get(index % mFilters.size()));
        mTvIndex.setText(index % mFilters.size() +"");
        index++;
    }

    public void switchCamera(View view){
        mCameraV1.switchCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraV1.onResume();
    }

    @Override
    protected void onPause() {
        mCameraV1.onPause();
        super.onPause();
    }
}
