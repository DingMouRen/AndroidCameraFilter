package com.dingmouren.camerafilter.listener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.wysaid.view.CameraRecordGLSurfaceView;

/**
 * Created by dingmouren
 * email: naildingmouren@gmail.com
 * github: https://github.com/DingMouRen
 */

public class EndRecordingFilterCallback implements CameraRecordGLSurfaceView.EndRecordingCallback {
    private Activity mActivity;
    private String mVideoFilePath;

    public EndRecordingFilterCallback(Activity activity){
        this.mActivity = activity;
    }

    public void setVideoFilePath(String videoFilePath){
        this.mVideoFilePath = videoFilePath;
    }
    @Override
    public void endRecordingOK() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity,"视频保存在:"+mVideoFilePath,Toast.LENGTH_SHORT).show();
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mVideoFilePath)));
            }
        });

    }
}
