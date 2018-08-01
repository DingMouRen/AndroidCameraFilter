package com.dingmouren.camerafilter.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.dingmouren.camerafilter.CameraOperator;

import java.io.File;
import java.io.IOException;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class LoadImageFileTask extends LoadImageTask {

    private final File mImageFile;

    public LoadImageFileTask(CameraOperator cameraOperator, File file) {
        super(cameraOperator);
        mImageFile = file;
    }

    @Override
    protected Bitmap decode(BitmapFactory.Options options) {
        return BitmapFactory.decodeFile(mImageFile.getAbsolutePath(), options);
    }

    @Override
    protected int getImageOrientation() throws IOException {
        ExifInterface exif = new ExifInterface(mImageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return 0;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}
