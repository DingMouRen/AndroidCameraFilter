package com.dingmouren.camerafilter.camera;

import android.app.Activity;
import android.hardware.Camera;

import com.dingmouren.camerafilter.CameraOperator;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class CameraV1 {
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
        // TODO adjust by getting supportedPreviewSizes and then choosing
        // the best one for screen size (best fill screen)
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
}
