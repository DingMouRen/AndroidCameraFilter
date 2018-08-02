package com.dingmouren.androidcamerafilter;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dingmouren.camerafilter.FilterImageActivity;

import org.wysaid.common.Common;
import org.wysaid.nativePort.CGENativeLibrary;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CGENativeLibrary.setLoadImageCallback(mLoadImageCallback, null);
        startActivity(new Intent(this, FilterImageActivity.class));
    }

    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {

        //Notice: the 'name' passed in is just what you write in the rule, e.g: 1.jpg
        //注意， 这里回传的name不包含任何路径名， 仅为具体的图片文件名如 1.jpg
        @Override
        public Bitmap loadImage(String name, Object arg) {

            Log.i(Common.LOG_TAG, "Loading file: " + name);
            AssetManager am = getAssets();
            InputStream is;
            try {
                is = am.open(name);
            } catch (IOException e) {
                Log.e(Common.LOG_TAG, "Can not open file " + name);
                return null;
            }

            return BitmapFactory.decodeStream(is);
        }

        @Override
        public void loadImageOK(Bitmap bmp, Object arg) {
            Log.i(Common.LOG_TAG, "Loading bitmap over, you can choose to recycle or cache");

            //The bitmap is which you returned at 'loadImage'.
            //You can call recycle when this function is called, or just keep it for further usage.
            //唯一不需要马上recycle的应用场景为 多个不同的滤镜都使用到相同的bitmap
            //那么可以选择缓存起来。
            bmp.recycle();
        }
    };
}
