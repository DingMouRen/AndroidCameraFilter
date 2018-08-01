package com.dingmouren.camerafilter.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;

import com.dingmouren.camerafilter.CameraOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class SaveImageTask extends AsyncTask<Void, Void, Void> {

    private final Bitmap mBitmap;
    private final String mFolderName;
    private final String mFileName;
    private final CameraOperator.OnPictureSavedListener mListener;
    private final Handler mHandler;
    private final CameraOperator mCameraOperator;
    private Context mContext;

    public SaveImageTask(final Bitmap bitmap,
                         final String folderName,
                         final String fileName,
                    CameraOperator cameraOperator,
                    final CameraOperator.OnPictureSavedListener listener) {
        mBitmap = bitmap;
        mFolderName = folderName;
        mFileName = fileName;
        mListener = listener;
        mCameraOperator = cameraOperator;
        mContext = mCameraOperator.getContext();
        mHandler = new Handler();
    }

    @Override
    protected Void doInBackground(final Void... params) {
        Bitmap result = mCameraOperator.getBitmapWithFilterApplied(mBitmap);
        saveImage(mFolderName, mFileName, result);
        return null;
    }

    private void saveImage(final String folderName, final String fileName, final Bitmap image) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, folderName + "/" + fileName);
        try {
            file.getParentFile().mkdirs();
            image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
            MediaScannerConnection.scanFile(mContext, new String[]{file.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(final String path, final Uri uri) {
                    if (mListener != null) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                mListener.onPictureSaved(uri, image);
                            }
                        });
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
