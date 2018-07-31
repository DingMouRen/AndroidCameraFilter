package com.dingmouren.camerafilter.camera;

import android.app.Activity;
import android.hardware.Camera;

import com.dingmouren.camerafilter.utils.GPUImage;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class CameraLoader {
    private int mCurrentCameraId = 0;
    private Camera mCameraInstance;
    private Activity mActivity;
    private CameraHelper mCameraHelper;
    private GPUImage mGPUImage;

    public CameraLoader(Activity mActivity, CameraHelper mCameraHelper, GPUImage mGPUImage) {
        this.mActivity = mActivity;
        this.mCameraHelper = mCameraHelper;
        this.mGPUImage = mGPUImage;
    }

    public void onResume() {
        setUpCamera(mCurrentCameraId);
    }

    public void onPause() {
        releaseCamera();
    }

    public void switchCamera() {
        releaseCamera();
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        setUpCamera(mCurrentCameraId);
    }

    private void setUpCamera(final int id) {
        mCameraInstance = getCameraInstance(id);
        Camera.Parameters parameters = mCameraInstance.getParameters();
        // TODO adjust by getting supportedPreviewSizes and then choosing
        // the best one for screen size (best fill screen)
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCameraInstance.setParameters(parameters);

        int orientation = mCameraHelper.getCameraDisplayOrientation(mActivity, mCurrentCameraId);
        CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
        mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
        boolean flipHorizontal = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(final int id) {
        Camera camera = null;
        try {
            camera = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    private void releaseCamera() {
        mCameraInstance.setPreviewCallback(null);
        mCameraInstance.release();
        mCameraInstance = null;
    }
}
