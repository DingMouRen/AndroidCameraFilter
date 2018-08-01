package com.dingmouren.camerafilter.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.dingmouren.camerafilter.CameraOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class CameraV1 {

    private static final String TAG = "CameraV1";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private int mCurrentCameraId = 0;
    private Camera mCamera;
    private Activity mActivity;
    private CameraV1Helper mCameraV1Helper;
    private CameraOperator mCameraOperator;



    public CameraV1(Activity mActivity, CameraV1Helper mCameraV1Helper, CameraOperator mCameraOperator) {
        this.mActivity = mActivity;
        this.mCameraV1Helper = mCameraV1Helper;
        this.mCameraOperator = mCameraOperator;
    }

    /**
     * 可见的情况下初始化摄像头
     */
    public void onResume() {
        initCamera(mCurrentCameraId);
    }

    /**
     * 不可见的情况下，释放摄像头资源
     */
    public void onPause() {
        releaseCamera();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        releaseCamera();
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraV1Helper.getNumberOfCameras();
        initCamera(mCurrentCameraId);
    }

    /**
     * 初始化摄像头
     * @param id
     */
    private void initCamera(final int id) {
        mCamera = getCamera(id);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(parameters);

        int orientation = mCameraV1Helper.getCameraDisplayOrientation(mActivity, mCurrentCameraId);
        CameraV1Helper.CameraInfo2 cameraInfo = new CameraV1Helper.CameraInfo2();
        mCameraV1Helper.getCameraInfo(mCurrentCameraId, cameraInfo);
        boolean flipHorizontal = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        mCameraOperator.setUpCamera(mCamera, orientation, flipHorizontal, false);
    }

    /**
     * 获取指定摄像头
     */
    private Camera getCamera(final int id) {
        Camera camera = null;
        try {
            camera = mCameraV1Helper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 获取当前摄像头
     * @return
     */
    public Camera getCamera(){
        if (mCamera != null){
            return mCamera;
        }else {
            return null;
        }
    }

    /**
     * 释放摄像头资源
     */
    private void releaseCamera() {
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    /**
     * 拍照
     */
    public void takePicture(final String folderName, final String fileName){
        if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            capturePicture(folderName,fileName);
        } else {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(final boolean success, final Camera camera) {
                    capturePicture(folderName,fileName);
                }
            });
        }
    }

    private void capturePicture(final String folderName, final String fileName) {
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(90);
        mCamera.setParameters(params);
        for (Camera.Size size : params.getSupportedPictureSizes()) {

        }
        mCamera.takePicture(null, null,
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {

                        final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            Log.d(TAG, "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }

                        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                        mCameraOperator.getGlSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        mCameraOperator.saveToPictures(bitmap, folderName, fileName,
                                new CameraOperator.OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri uri, final Bitmap img) {

                                        Toast.makeText(mActivity,mCameraOperator.getUriPath(uri),Toast.LENGTH_SHORT).show();
                                        pictureFile.delete();
                                        camera.startPreview();
                                        mCameraOperator.getGlSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                    }
                                });
                    }
                });
    }



    private static File getOutputMediaFile(final int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
