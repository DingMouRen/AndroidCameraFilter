package com.dingmouren.camerafilter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dingmouren.camerafilter.callback.LoadAssetsImageCallback;
import com.dingmouren.camerafilter.dialog.DialogFilter;
import com.dingmouren.camerafilter.util.ConvertBitmapUtils;

import org.wysaid.nativePort.CGENativeLibrary;
import org.wysaid.view.ImageGLSurfaceView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class FilterImageActivity extends AppCompatActivity {

    private static final String TAG = "FilterImageActivity";
    public static final String BITMAP_UN_FILTER = "bitmap_un_filter";
    public static final String BITMAP_FILTER = "bitmap_filtert";
    public static final int REQUEST_CODE_FILTER_IMG = 101;

    private ImageGLSurfaceView mGLSurfaceView;
    private ImageView mImgFilter;
    private ImageView mImgCancel;
    private ImageView mImgConfirm;

    private Bitmap mBitmap;
    private DialogFilter mDialogFilter;

    public static void disposeBitmap(Activity activity, byte[] bytesArray){
        Intent intent = new Intent(activity,FilterImageActivity.class);
        intent.putExtra(BITMAP_UN_FILTER,bytesArray);
        activity.startActivityForResult(intent,REQUEST_CODE_FILTER_IMG);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_image);
        CGENativeLibrary.setLoadImageCallback(new LoadAssetsImageCallback(this), null);
        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img_girl);

        if (getIntent() != null){
            byte[] bytesArray = getIntent().getByteArrayExtra(BITMAP_UN_FILTER);
            if (null != bytesArray && bytesArray.length > 0)
            mBitmap = BitmapFactory.decodeByteArray(bytesArray,0,bytesArray.length);
        }

        initView();

        initListener();
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mImgFilter = findViewById(R.id.img_filter);
        mImgCancel = findViewById(R.id.img_cancel);
        mImgConfirm = findViewById(R.id.img_confirm);

        mDialogFilter = new DialogFilter(this);
    }

    private void initListener(){
        /*GLSurfaceView创建时的监听*/
        mGLSurfaceView.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
            @Override
            public void surfaceCreated() {
                mGLSurfaceView.setImageBitmap(mBitmap);
                mGLSurfaceView.setDisplayMode(ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT);
            }
        });
        /*显示原图*/
        mGLSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView.setFilterWithConfig(ConstantFilters.FILTERS[0]);
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
                mGLSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,ConstantFilters.FILTERS[position]);
                        mGLSurfaceView.setFilterWithConfig(ConstantFilters.FILTERS[position]);
                    }
                });
            }
        });
        /*过滤对话框显示的监听*/
        mDialogFilter.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mImgFilter.animate().alpha(0).setDuration(1000).start();
            }
        });
        /*过滤对话框隐藏的监听*/
        mDialogFilter.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mImgFilter.animate().alpha(1).setDuration(1000).start();
            }
        });
        /*关闭*/
        mImgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*返回滤镜处理过的bitmap*/
        mImgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLSurfaceView.getResultBitmap(new ImageGLSurfaceView.QueryResultBitmapCallback() {
                    @Override
                    public void get(final Bitmap bmp) {
                        Intent intent = new Intent();
                        intent.putExtra(BITMAP_FILTER, ConvertBitmapUtils.bitmapToByteArray(bmp));
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmap != null){
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
