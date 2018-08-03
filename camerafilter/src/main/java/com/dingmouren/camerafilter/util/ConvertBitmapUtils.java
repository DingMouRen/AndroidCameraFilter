package com.dingmouren.camerafilter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class ConvertBitmapUtils {

    /**
     * Bitmap转字节数组
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap){
        if (null == bitmap) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        return baos.toByteArray();
    }

    /**
     * 字节数组转Bitmap
     * @param bytesArray
     * @return
     */
    public static Bitmap byteArrayToBitmap(byte[] bytesArray){
        if (null == bytesArray || bytesArray.length <= 0) return null;
        return BitmapFactory.decodeByteArray(bytesArray,0,bytesArray.length);
    }
}
