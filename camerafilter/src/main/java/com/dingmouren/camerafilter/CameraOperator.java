/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dingmouren.camerafilter;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;


import com.dingmouren.camerafilter.filter.FilterBase;
import com.dingmouren.camerafilter.task.LoadImageFileTask;
import com.dingmouren.camerafilter.task.LoadImageUriTask;
import com.dingmouren.camerafilter.task.SaveImageTask;
import com.dingmouren.camerafilter.utils.PixelBuffer;
import com.dingmouren.camerafilter.utils.Rotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * The main accessor for CameraOperator functionality. This class helps to do common
 * tasks through a simple interface.
 */
public class CameraOperator {
    private final Context mContext;
    private final CameraRenderer mRenderer;
    private GLSurfaceView mGlSurfaceView;
    private FilterBase mFilter;
    private Bitmap mCurrentBitmap;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    public CameraOperator(final Context context) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        mContext = context;
        mFilter = new FilterBase();
        mRenderer = new CameraRenderer(mFilter);
    }

    /**
     * 检查当前设备是否支持OpenGL ES 2.0。
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * 设置将显示预览的GLSurfaceView。
     *
     * @param glSurfaceView the GLSurfaceView
     */
    public void setGLSurfaceView(final GLSurfaceView glSurfaceView) {
        mGlSurfaceView = glSurfaceView;
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlSurfaceView.requestRender();
    }

    /**
     * 设置清屏颜色
     *
     * @param red   red color value
     * @param green green color value
     * @param blue  red color value
     */
    public void setBackgroundColor(float red, float green, float blue) {
        mRenderer.setBackgroundColor(red, green, blue);
    }

    /**
     * 请求再次渲染预览。
     */
    public void requestRender() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.requestRender();
        }
    }


    /**
     * 设置要连接到GPUImage的摄像头以获得过滤预览。
     *
     * @param camera         the camera
     * @param degrees        通过旋转图像的度数
     * @param flipHorizontal 如果图像应水平翻转
     * @param flipVertical   如果图像应垂直翻转
     */
    public void setUpCamera(final Camera camera, final int degrees, final boolean flipHorizontal,
                            final boolean flipVertical) {
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) { // 大于level 10
            setUpSurfaceTexture(camera);
        } else { // 小于level 10
            camera.setPreviewCallback(mRenderer);
            camera.startPreview();
        }
        Rotation rotation = Rotation.NORMAL;
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
        mRenderer.setRotationCamera(rotation, flipHorizontal, flipVertical);
    }

    /**
     * 设置相机预览的surfacetexture
     *
     * @param camera
     */
    @TargetApi(11)
    private void setUpSurfaceTexture(final Camera camera) {
        mRenderer.setUpSurfaceTexture(camera);
    }

    /**
     * 设置滤镜
     *
     * @param filter the new filter
     */
    public void setFilter(final FilterBase filter) {
        mFilter = filter;
        mRenderer.setFilter(mFilter);
        requestRender();
    }

    /**
     * Sets the image on which the filter should be applied.
     * 设置应在其上应用滤镜的图像。
     *
     * @param bitmap the new image
     */
    public void setImage(final Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }

    /**
     * This sets the scale type of CameraOperator. This has to be run before setting the image.
     * If image is set and scale type changed, image needs to be reset.
     * 这将设置GPUImage的缩放类型。, 必须在设置图像之前运行。 , *如果设置了图像并更改了比例类型，则需要重置图像
     *
     * @param scaleType The new ScaleType
     */
    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        mRenderer.setScaleType(scaleType);
        mRenderer.deleteImage();
        mCurrentBitmap = null;
        requestRender();
    }

    /**
     * 设置显示图像的旋转。
     *
     * @param rotation new rotation
     */
    public void setRotation(Rotation rotation) {
        mRenderer.setRotation(rotation);
    }

    /**
     * 使用翻转选项设置显示图像的旋转。
     *
     * @param rotation new rotation
     */
    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        mRenderer.setRotation(rotation, flipHorizontal, flipVertical);
    }

    /**
     * 删除当前图像
     */
    public void deleteImage() {
        mRenderer.deleteImage();
        mCurrentBitmap = null;
        requestRender();
    }

    /**
     * 设置应从Uri应用滤镜的图像
     *
     * @param uri the uri of the new image
     */
    public void setImage(final Uri uri) {
        new LoadImageUriTask(this, uri).execute();
    }

    /**
     * 设置应从File应用过滤器的图像。
     *
     * @param file the file of the new image
     */
    public void setImage(final File file) {
        new LoadImageFileTask(this, file).execute();
    }

    /**
     * 获取uri的路径
     *
     * @param uri
     * @return
     */
    private String getPath(final Uri uri) {
        String[] projection = {
                MediaStore.Images.Media.DATA,
        };
        Cursor cursor = mContext.getContentResolver()
                .query(uri, projection, null, null, null);
        int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(pathIndex);
        }
        cursor.close();
        return path;
    }

    /**
     * 给当前位图添加滤镜
     *
     * @return the current image with filter applied
     */
    public Bitmap getBitmapWithFilterApplied() {
        return getBitmapWithFilterApplied(mCurrentBitmap);
    }

    /**
     * 给bitamp添加滤镜
     */
    public Bitmap getBitmapWithFilterApplied(final Bitmap bitmap) {
        if (mGlSurfaceView != null) {
            mRenderer.deleteImage();
            mRenderer.runOnDraw(new Runnable() {

                @Override
                public void run() {
                    synchronized (mFilter) {
                        mFilter.destroy();
                        mFilter.notify();
                    }
                }
            });
            synchronized (mFilter) {
                requestRender();
                try {
                    mFilter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        CameraRenderer renderer = new CameraRenderer(mFilter);
        renderer.setRotation(Rotation.NORMAL,
                mRenderer.isFlippedHorizontally(), mRenderer.isFlippedVertically());
        renderer.setScaleType(mScaleType);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(bitmap, false);
        Bitmap resultBitmap = buffer.getBitmap();
        mFilter.destroy();
        renderer.deleteImage();
        buffer.destroy();

        mRenderer.setFilter(mFilter);
        if (mCurrentBitmap != null) {
            mRenderer.setImageBitmap(mCurrentBitmap, false);
        }
        requestRender();

        return resultBitmap;
    }

    /**
     * Gets the images for multiple filters on a image. This can be used to
     * quickly get thumbnail images for filters. <br>
     * Whenever a new Bitmap is ready, the listener will be called with the
     * bitmap. The order of the calls to the listener will be the same as the
     * filter order.
     *
     * @param bitmap   the bitmap on which the filters will be applied
     * @param filters  the filters which will be applied on the bitmap
     * @param listener the listener on which the results will be notified
     */
    public static void getBitmapForMultipleFilters(final Bitmap bitmap,
                                                   final List<FilterBase> filters, final ResponseListener<Bitmap> listener) {
        if (filters.isEmpty()) {
            return;
        }
        CameraRenderer renderer = new CameraRenderer(filters.get(0));
        renderer.setImageBitmap(bitmap, false);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);

        for (FilterBase filter : filters) {
            renderer.setFilter(filter);
            listener.response(buffer.getBitmap());
            filter.destroy();
        }
        renderer.deleteImage();
        buffer.destroy();
    }

    /**
     * Save current image with applied filter to Pictures. It will be stored on
     * the default Picture folder on the phone below the given folderName and
     * fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param folderName the folder name
     * @param fileName   the file name
     * @param listener   the listener
     */
    @Deprecated
    public void saveToPictures(final String folderName, final String fileName,
                               final OnPictureSavedListener listener) {
        saveToPictures(mCurrentBitmap, folderName, fileName, listener);
    }

    /**
     * Apply and save the given bitmap with applied filter to Pictures. It will
     * be stored on the default Picture folder on the phone below the given
     * folerName and fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param bitmap     the bitmap
     * @param folderName the folder name
     * @param fileName   the file name
     * @param listener   the listener
     */
    @Deprecated
    public void saveToPictures(final Bitmap bitmap, final String folderName, final String fileName,
                               final OnPictureSavedListener listener) {
        new SaveImageTask(bitmap, folderName, fileName, this,listener).execute();
    }

    /**
     * 在OpenGL线程上运行给定的Runnable
     *
     * @param runnable The runnable to be run on the OpenGL thread.
     */
    public void runOnGLThread(Runnable runnable) {
        mRenderer.runOnDrawEnd(runnable);
    }


    /**
     * 获取输出的宽度
     *
     * @return
     */
    public int getOutputWidth() {
        if (mRenderer != null && mRenderer.getFrameWidth() != 0) {
            return mRenderer.getFrameWidth();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getWidth();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getWidth();
        }
    }

    /**
     * 获取输出的高度
     *
     * @return
     */
    public int getOutputHeight() {
        if (mRenderer != null && mRenderer.getFrameHeight() != 0) {
            return mRenderer.getFrameHeight();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getHeight();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getHeight();
        }
    }


    /**
     * 图片保存结束后的监听
     */
    public interface OnPictureSavedListener {
        void onPictureSaved(Uri uri, final Bitmap image);
    }


    public CameraRenderer getCameraRenderer() {
        return mRenderer;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public Context getContext() {
        return mContext;
    }

    public interface ResponseListener<T> {
        void response(T item);
    }

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP
    }
}
