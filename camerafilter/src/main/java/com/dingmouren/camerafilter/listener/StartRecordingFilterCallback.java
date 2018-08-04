package com.dingmouren.camerafilter.listener;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.wysaid.view.CameraRecordGLSurfaceView;

/**
 * Created by dingmouren
 * email: naildingmouren@gmail.com
 * github: https://github.com/DingMouRen
 */

public class StartRecordingFilterCallback implements CameraRecordGLSurfaceView.StartRecordingCallback {

    private Activity mActivity;

    public StartRecordingFilterCallback(Activity activity){
        this.mActivity = activity;
    }

    @Override
    public void startRecordingOver(final boolean success) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success){
                    Toast.makeText(mActivity,"开始录制视频",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(mActivity,"录制视频失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
