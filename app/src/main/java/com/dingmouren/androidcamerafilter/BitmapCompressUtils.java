package com.dingmouren.androidcamerafilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by dingmouren
 * email: naildingmouren@gmail.com
 * github: https://github.com/DingMouRen
 * Bitmap压缩优化
 */

public class BitmapCompressUtils {

    private static final String TAG = "BitmapCompressUtils";

    /**
     * 质量压缩
     * png图片是无损的，不能进行质量压缩
     */
    public static Bitmap compressQuality(Context context, int resId, int quality) {
         /*原bitmap与压缩后的bitmap的声明*/
        Bitmap bitmapOut = null;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        /*获取图片的格式类型*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        String mimeType = options.outMimeType;
        Log.i(TAG, "图片格式:" + mimeType);
        if (quality < 0 || quality > 100) {
            Log.e(TAG, "图片质量要在0-100之间");
            return null;
        }

        /*分别对jpeg与png进行质量压缩处理*/
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (TextUtils.equals(mimeType,"image/jpeg")) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        byte[] bytes = baos.toByteArray();
        bitmapOut = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Log.i(TAG, "compressQuality--原图片的大小：" + (bitmap.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmap.getWidth() + " 高度为" + bitmap.getHeight());

        Log.i(TAG, "compressQuality--压缩后图片的大小：" + (bitmapOut.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmapOut.getWidth() + " 高度为" + bitmapOut.getHeight()
                + " bytes.length=" + (bytes.length / 1024) + "KB"
                + " quality=" + quality);
        bitmap.recycle();
        return bitmapOut;
    }

    /**
     * 采样率压缩
     */
    public static Bitmap compressSampling(Context context,int resId,int sampleSize){
        Bitmap bitmapOut = null;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        bitmapOut = BitmapFactory.decodeResource(context.getResources(),resId,options);
        Log.i(TAG, "compressSampling--原图片的大小：" + (bitmap.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmap.getWidth() + " 高度为" + bitmap.getHeight());

        Log.i(TAG, "compressSampling--压缩后图片的大小：" + (bitmapOut.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmapOut.getWidth() + " 高度为" + bitmapOut.getHeight()
               );
        bitmap.recycle();
        return bitmapOut;
    }

    /**
     * 缩放法压缩
     */
    public static Bitmap compressScale(Context context,int resId,float scaleX,float scaleY){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId);

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX,scaleY);
        Bitmap bitmapOut = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        Log.i(TAG, "compressScale--压缩后图片的大小：" + (bitmapOut.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmapOut.getWidth() + " 高度为" + bitmapOut.getHeight()
        );
        bitmap.recycle();
        return bitmapOut;
    }

    /**
     * 设置压缩格式来压缩
     */
    public static Bitmap compressConfig(Context context,int resId){
        Bitmap bitmapOut = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmapOut = BitmapFactory.decodeResource(context.getResources(),resId,options);
        Log.i(TAG, "compressConfig--压缩后图片的大小：" + (bitmapOut.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmapOut.getWidth() + " 高度为" + bitmapOut.getHeight()
        );
        return bitmapOut;
    }

    /**
     * 创建新的Bitmap，并指定图片的宽高
     */
    public static Bitmap compressCreateScaleBitmap(Context context,int resId,int width,int height){
        Bitmap bitmapOut = null;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId);
        bitmapOut = Bitmap.createScaledBitmap(bitmap,width,height,true);
        Log.i(TAG, "compressCreateScaleBitmap--压缩后图片的大小：" + (bitmapOut.getByteCount() / 1024 / 1024)
                + "M 宽度为" + bitmapOut.getWidth() + " 高度为" + bitmapOut.getHeight()
        );
        bitmap.recycle();
        return bitmapOut;
    }
}
