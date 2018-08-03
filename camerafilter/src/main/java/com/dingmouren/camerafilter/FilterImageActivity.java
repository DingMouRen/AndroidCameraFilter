package com.dingmouren.camerafilter;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dingmouren.camerafilter.dialog.DialogFilter;

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

    private ImageGLSurfaceView mGLSurfaceView;
    private CircleImageView mImgFilter;

    private Bitmap mBitmap;
    private DialogFilter mDialogFilter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_image);
        CGENativeLibrary.setLoadImageCallback(new LoadAssetsImageCallback(this), null);
        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img_girl);

        initView();

        initListener();
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mImgFilter = findViewById(R.id.img_filter);

        mDialogFilter = new DialogFilter(this);
    }

    private void initListener(){
        mGLSurfaceView.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
            @Override
            public void surfaceCreated() {
                mGLSurfaceView.setImageBitmap(mBitmap);
                mGLSurfaceView.setDisplayMode(ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT);
            }
        });

        mGLSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView.setFilterWithConfig(ConstantFilters.FILTERS[0]);
            }
        });

        mImgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFilter.show();
            }
        });

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

        mDialogFilter.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mImgFilter.animate().alpha(0).setDuration(300).start();
            }
        });
        mDialogFilter.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mImgFilter.animate().alpha(1).setDuration(300).start();
            }
        });


    }
}
