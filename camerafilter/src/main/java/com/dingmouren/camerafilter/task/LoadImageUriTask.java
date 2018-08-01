package com.dingmouren.camerafilter.task;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.dingmouren.camerafilter.CameraOperator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class LoadImageUriTask extends LoadImageTask {

    private final Uri mUri;
    private Context mContext;

    public LoadImageUriTask(CameraOperator cameraOperator, Uri uri) {
        super(cameraOperator);
        mUri = uri;
        mContext = cameraOperator.getContext();
    }

    @Override
    protected Bitmap decode(BitmapFactory.Options options) {
        try {
            InputStream inputStream;
            if (mUri.getScheme().startsWith("http") || mUri.getScheme().startsWith("https")) {
                inputStream = new URL(mUri.toString()).openStream();
            } else {
                inputStream = mContext.getContentResolver().openInputStream(mUri);
            }
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected int getImageOrientation() throws IOException {
        Cursor cursor = mContext.getContentResolver().query(mUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            return 0;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }
}
