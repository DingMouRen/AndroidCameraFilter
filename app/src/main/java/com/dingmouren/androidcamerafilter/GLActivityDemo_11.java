package com.dingmouren.androidcamerafilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


import com.dingmouren.camerafilter.GPUImageView;
import com.dingmouren.camerafilter.camera.CameraHelper;
import com.dingmouren.camerafilter.camera.CameraLoader;
import com.dingmouren.camerafilter.filter.FilterBase;
import com.dingmouren.camerafilter.filter.FilterColorInvert;
import com.dingmouren.camerafilter.filter.FilterPixelation;
import com.dingmouren.camerafilter.utils.GPUImage;

import java.io.File;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class GLActivityDemo_11 extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private GPUImage mGPUImage;

    private CameraHelper mCameraHelper;
    private CameraLoader mCameraLoader;

    private FilterPixelation filterPixelation;
    private FilterColorInvert filterColorInvert;
    private FilterBase filterBase;
    private int index = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_demo11);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);



        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(mGLSurfaceView);

        mCameraHelper = new CameraHelper(this);

        mCameraLoader = new CameraLoader(this,mCameraHelper,mGPUImage);
        filterBase = new FilterBase();
        filterPixelation = new FilterPixelation();
        filterColorInvert = new FilterColorInvert();
        mGPUImage.setFilter(filterBase);

    }

    public void changeFilter(View view) {
        switch (index % 3) {
            case 0:
                mGPUImage.setFilter(filterBase);
                break;
            case 1:
                mGPUImage.setFilter(filterPixelation);
                break;
            case 2:
                mGPUImage.setFilter(filterColorInvert);
                break;
        }
        index++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraLoader.onResume();
    }

    @Override
    protected void onPause() {
        mCameraLoader.onPause();
        super.onPause();
    }




    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }


    }
}
