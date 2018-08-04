package com.dingmouren.camerafilter.listener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.dingmouren.camerafilter.FilterCameraActivity;

import org.wysaid.myUtils.ImageUtil;
import org.wysaid.view.CameraRecordGLSurfaceView;

/**
 * Created by dingmouren
 * email: naildingmouren@gmail.com
 * github: https://github.com/DingMouRen
 */

public class TakePhotoFilterCallback implements CameraRecordGLSurfaceView.TakePictureCallback {

    private Context mContext;

    public TakePhotoFilterCallback(Context context){
        this.mContext = context;
    }

    @Override
    public void takePictureOK(Bitmap bmp) {
        if (bmp != null) {
            String s = ImageUtil.saveBitmap(bmp);
            bmp.recycle();
            Toast.makeText(mContext,"图片保存在:"+s,Toast.LENGTH_SHORT).show();
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
        } else{
            Toast.makeText(mContext,"拍照失败",Toast.LENGTH_SHORT).show();
        }
    }
}
