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
import android.widget.Toast;


import com.dingmouren.camerafilter.camera.CameraV1Helper;
import com.dingmouren.camerafilter.camera.CameraV1;
import com.dingmouren.camerafilter.filter.FilterBase;
import com.dingmouren.camerafilter.filter.FilterColorInvert;
import com.dingmouren.camerafilter.filter.FilterPixelation;
import com.dingmouren.camerafilter.CameraOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private ImageView img;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_demo11);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        img = findViewById(R.id.img);


        mCameraOperator = new CameraOperator(this);
        mCameraOperator.setGLSurfaceView(mGLSurfaceView);

        mCameraV1Helper = new CameraV1Helper(this);
        mCameraV1 = new CameraV1(this, mCameraV1Helper, mCameraOperator);

        FilterBase filterBase = new FilterBase();
        FilterPixelation filterPixelation = new FilterPixelation();
        FilterColorInvert filterColorInvert = new FilterColorInvert();

        mFilters.add(filterBase);
        mFilters.add(filterPixelation);
        mFilters.add(filterColorInvert);

        mCameraOperator.setFilter(filterBase);


    }

    public void getPhoto(View view){
        if (mCameraV1.getCamera().getParameters().getFocusMode().equals(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            takePicture();
        } else {
            mCameraV1.getCamera().autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(final boolean success, final Camera camera) {
                    takePicture();
                }
            });
        }
    }


    private void takePicture() {
        // TODO get a size that is about the size of the screen
        Camera.Parameters params = mCameraV1.getCamera().getParameters();
        params.setRotation(90);
        mCameraV1.getCamera().setParameters(params);
        for (Camera.Size size : params.getSupportedPictureSizes()) {

        }
        mCameraV1.getCamera().takePicture(null, null,
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {

                        final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            Log.d("ASDF",
                                    "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("ASDF", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("ASDF", "Error accessing file: " + e.getMessage());
                        }

                        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                        /*在这里的bitmap是没有滤镜的*/
//                        img.setImageBitmap(bitmap);
                        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        mCameraOperator.saveToPictures(bitmap, "GPUImage",
                                System.currentTimeMillis() + ".jpg",
                                new CameraOperator.OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri uri,final Bitmap bitmap) {
                                        /*这里的bitmao是经过滤镜处理过的*/
                                        img.setImageBitmap(bitmap);
                                        Toast.makeText(GLActivityDemo_11.this,"图片保存在:"+uri.getPath(),Toast.LENGTH_SHORT).show();
                                        pictureFile.delete();
                                        camera.startPreview();
                                        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                    }
                                });
                    }
                });
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static File getOutputMediaFile(final int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    public void changeFilter(View view) {
        mCameraOperator.setFilter(mFilters.get(index%3));
        index++;
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
