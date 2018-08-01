package com.dingmouren.camerafilter.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import com.dingmouren.camerafilter.CameraOperator;
import com.dingmouren.camerafilter.CameraRenderer;

import java.io.IOException;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public abstract class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

    private final CameraOperator mCameraOperator;
    private int mOutputWidth;
    private int mOutputHeight;
    private CameraRenderer mRenderer;
    private CameraOperator.ScaleType mScaleType;

    @SuppressWarnings("deprecation")
    public LoadImageTask(final CameraOperator cameraOperator) {
        mCameraOperator = cameraOperator;
        mRenderer = mCameraOperator.getCameraRenderer();
        mScaleType = mCameraOperator.getScaleType();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (mRenderer != null && mRenderer.getFrameWidth() == 0) {
            try {
                synchronized (mRenderer.mSurfaceChangedWaiter) {
                    mRenderer.mSurfaceChangedWaiter.wait(3000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mOutputWidth = mCameraOperator.getOutputWidth();
        mOutputHeight = mCameraOperator.getOutputHeight();
        return loadResizedImage();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mCameraOperator.deleteImage();
        mCameraOperator.setImage(bitmap);
    }

    protected abstract Bitmap decode(BitmapFactory.Options options);

    private Bitmap loadResizedImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decode(options);
        int scale = 1;
        while (checkSize(options.outWidth / scale > mOutputWidth, options.outHeight / scale > mOutputHeight)) {
            scale++;
        }

        scale--;
        if (scale < 1) {
            scale = 1;
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inTempStorage = new byte[32 * 1024];
        Bitmap bitmap = decode(options);
        if (bitmap == null) {
            return null;
        }
        bitmap = rotateImage(bitmap);
        bitmap = scaleBitmap(bitmap);
        return bitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        // resize to desired dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] newSize = getScaleSize(width, height);
        Bitmap workBitmap = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], true);
        if (workBitmap != bitmap) {
            bitmap.recycle();
            bitmap = workBitmap;
            System.gc();
        }

        if (mScaleType == CameraOperator.ScaleType.CENTER_CROP) {
            // Crop it
            int diffWidth = newSize[0] - mOutputWidth;
            int diffHeight = newSize[1] - mOutputHeight;
            workBitmap = Bitmap.createBitmap(bitmap, diffWidth / 2, diffHeight / 2,
                    newSize[0] - diffWidth, newSize[1] - diffHeight);
            if (workBitmap != bitmap) {
                bitmap.recycle();
                bitmap = workBitmap;
            }
        }

        return bitmap;
    }

    /**
     * Retrieve the scaling size for the image dependent on the ScaleType.<br>
     *     根据ScaleType检索图像的缩放大小
     * <br>
     * If CROP: sides are same size or bigger than output's sides<br>
     * Else   : sides are same size or smaller than output's sides
     */
    private int[] getScaleSize(int width, int height) {
        float newWidth;
        float newHeight;

        float withRatio = (float) width / mOutputWidth;
        float heightRatio = (float) height / mOutputHeight;

        boolean adjustWidth = mScaleType == CameraOperator.ScaleType.CENTER_CROP ? withRatio > heightRatio : withRatio < heightRatio;

        if (adjustWidth) {
            newHeight = mOutputHeight;
            newWidth = (newHeight / height) * width;
        } else {
            newWidth = mOutputWidth;
            newHeight = (newWidth / width) * height;
        }
        return new int[]{Math.round(newWidth), Math.round(newHeight)};
    }

    /**/
    private boolean checkSize(boolean widthBigger, boolean heightBigger) {
        if (mScaleType == CameraOperator.ScaleType.CENTER_CROP) {
            return widthBigger && heightBigger;
        } else {
            return widthBigger || heightBigger;
        }
    }

    /**/
    private Bitmap rotateImage(final Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap rotatedBitmap = bitmap;
        try {
            int orientation = getImageOrientation();
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                bitmap.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    protected abstract int getImageOrientation() throws IOException;
}
