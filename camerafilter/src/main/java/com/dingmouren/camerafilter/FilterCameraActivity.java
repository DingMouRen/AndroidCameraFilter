package com.dingmouren.camerafilter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dingmouren.camerafilter.callback.LoadAssetsImageCallback;
import com.dingmouren.camerafilter.dialog.DialogFilter;
import com.dingmouren.camerafilter.listener.EndRecordingFilterCallback;
import com.dingmouren.camerafilter.listener.StartRecordingFilterCallback;
import com.dingmouren.camerafilter.listener.TakePhotoFilterCallback;
import com.dingmouren.camerafilter.view.VideoControlView;

import org.wysaid.camera.CameraInstance;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.nativePort.CGENativeLibrary;
import org.wysaid.view.CameraRecordGLSurfaceView;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class FilterCameraActivity extends AppCompatActivity {

    private static final String TAG = FilterCameraActivity.class.getName();

    private CameraRecordGLSurfaceView mCameraView;
    private RelativeLayout mRelaBottom;
    private ImageView mImgFlash;
    private ImageView mImgFilter;
    private VideoControlView mVideoControlView;
    private ImageView mImgSwitch;

    private DialogFilter mDialogFilter;
    private  boolean mIsFlashOpened = false;
    private String mCurrentFilter;
    private TakePhotoFilterCallback mTakePhotoFilterCallback;/*拍照的回调*/
    private StartRecordingFilterCallback mStartRecordingFilterCallback;/*开始录制视频的回调*/
    private EndRecordingFilterCallback mEndRecordingFilterCallback;/*录制视频结束的回调*/

    private String[] mPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_camera);

        CGENativeLibrary.setLoadImageCallback(new LoadAssetsImageCallback(this), null);

        for (int i = 0; i < mPermissions.length; i++) {
            if (PermissionChecker.checkSelfPermission(this,mPermissions[i]) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{mPermissions[i]},1);
            }
        }

        initView();

        initListener();

    }

    private void initView() {
        mCameraView = findViewById(R.id.camera_view);
        mRelaBottom = findViewById(R.id.rela_bottom);
        mImgFlash = findViewById(R.id.img_flash);
        mImgFilter = findViewById(R.id.img_filter);
        mVideoControlView = findViewById(R.id.video_control_view);
        mImgSwitch = findViewById(R.id.img_switch);

        mDialogFilter = new DialogFilter(this);

         /*设置摄像头方向*/
        mCameraView.presetCameraForward(true);
        /*录制视频大小*/
        mCameraView.presetRecordingSize(480, 640);
         /*拍照大小。*/
        mCameraView.setPictureSize(2048, 2048, true);
        /*充满view*/
        mCameraView.setFitFullView(true);
        /*设置图片保存的目录*/
        ImageUtil.setDefaultFolder("dingmouren");

        mTakePhotoFilterCallback = new TakePhotoFilterCallback(this);
        mStartRecordingFilterCallback = new StartRecordingFilterCallback(this);
        mEndRecordingFilterCallback = new EndRecordingFilterCallback(this);
    }

    private void initListener() {
        /*切换前后摄像头*/
        mImgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.switchCamera();
            }
        });
        /*闪关灯开关*/
        mImgFlash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsFlashOpened){
                    mCameraView.setFlashLightMode(Camera.Parameters.FLASH_MODE_OFF);
                    mIsFlashOpened = false;
                }else {
                    mCameraView.setFlashLightMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mIsFlashOpened = true;
                }
            }
        });

        /*触摸对焦*/
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        Log.i(TAG, String.format("Tap to focus: %g, %g", event.getX(), event.getY()));
                        final float focusX = event.getX() / mCameraView.getWidth();
                        final float focusY = event.getY() / mCameraView.getHeight();

                        mCameraView.focusAtPoint(focusX, focusY, new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) {
                                    Log.e(TAG, String.format("Focus OK, pos: %g, %g", focusX, focusY));
                                } else {
                                    Log.e(TAG, String.format("Focus failed, pos: %g, %g", focusX, focusY));
                                    mCameraView.cameraInstance().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                                }
                            }
                        });
                    }
                    break;
                    default:
                        break;
                }

                return true;
            }
        });
        /*弹出选择滤镜的对话框*/
        mImgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFilter.show();
            }
        });

        /*滤镜对话框选择滤镜的监听*/
        mDialogFilter.setOnFilterChangedListener(new DialogFilter.OnFilterChangedListener() {
            @Override
            public void onFilterChangedListener(final int position) {
                mCameraView.setFilterWithConfig(ConstantFilters.FILTERS[position]);
                mCurrentFilter = ConstantFilters.FILTERS[position];
            }
        });

         /*过滤对话框显示的监听*/
        mDialogFilter.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mRelaBottom.animate().alpha(0).setDuration(1000).start();
            }
        });
        /*过滤对话框隐藏的监听*/
        mDialogFilter.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mRelaBottom.animate().alpha(1).setDuration(1000).start();
            }
        });

        /*拍照 录制*/
        mVideoControlView.setOnRecordListener(new VideoControlView.OnRecordListener() {
            @Override
            public void onShortClick() {
                mCameraView.takePicture(mTakePhotoFilterCallback, null, mCurrentFilter, 1.0f, true);
            }

            @Override
            public void OnRecordStartClick() {
                String videoFileName = ImageUtil.getPath()+ "/" + System.currentTimeMillis()+".mp4";
                mEndRecordingFilterCallback.setVideoFilePath(videoFileName);
                mCameraView.startRecording(videoFileName,mStartRecordingFilterCallback);
            }

            @Override
            public void OnFinish(int resultCode) {
                switch (resultCode) {
                    case 0:
                        Toast.makeText(FilterCameraActivity.this, "录制时间过短", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mCameraView.endRecording(mEndRecordingFilterCallback);
                        break;
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.resumePreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.release(null);
        CameraInstance.getInstance().stopCamera();
    }
}
