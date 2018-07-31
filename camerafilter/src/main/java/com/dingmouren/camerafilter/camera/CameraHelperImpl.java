package com.dingmouren.camerafilter.camera;

import android.hardware.Camera;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public interface CameraHelperImpl {
    /*获取摄像头的数量*/
    int getNumberOfCameras();

    /*开启指定摄像头*/
    Camera openCamera(int id);

    /*开启默认摄像头，默认是后置摄像头*/
    Camera openDefaultCamera();

    Camera openCameraFacing(int facing);

    /*是否有指定的摄像头*/
    boolean hasCamera(int cameraFacingFront);

    /*获取摄像头的信息*/
    void getCameraInfo(int cameraId, CameraHelper.CameraInfo2 cameraInfo);
}
