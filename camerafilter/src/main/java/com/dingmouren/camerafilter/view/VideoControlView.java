package com.dingmouren.camerafilter.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dingmouren.camerafilter.R;

import java.lang.ref.WeakReference;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class VideoControlView extends View {

    private static final String TAG = VideoControlView.class.getName();
    /**
     * 视频录制最长时间 秒
     */
    private static final int VIDEO_RECORD_DEFAULT_MAX_TIME = 10;
    /**
     * 视频录制最小时间 秒
     */
    private final int VIDEO_RECORD_DEFAULT_MIN_TIME = 1;
    /**
     * 视频录制内圆半径
     */
    private final float VIDEO_RECORD_DEFAULT_INNER_CIRCLE_RADIUS = 5f;
    /**
     * 视频录制外圆半径
     */
    private final float VIDEO_RECORD_DEFAULT_EXCIRCLE_RADIUS = 12f;
    /**
     * 视频录制圆环默认颜色
     */
    private final int VIDEO_RECORD_DEFAULT_ANNULUS_COLOR = Color.parseColor("#FFFFFF");
    /**
     * 视频录制内圆默认颜色
     */
    private final int VIDEO_RECORD_DEFAULT_INNER_CIRCLE_COLOR = Color.parseColor("#F5F5F5");
    /**
     * 视频录制进度默认颜色
     */
    private final int VIDEO_RECORD_DEFAULT_PROGRESS_COLOR = Color.parseColor("#00A653");

    /**
     * 外圆放大倍数
     */
    private final float EXCICLE_MAGNIFICATION = 1.25f;
    private float excicleMagnification;

    /**
     * 内圆缩小倍数
     */
    private final float INNER_CIRCLE_SHRINKS = 0.75f;
    private float innerCircleShrinks;
    /**
     * 视频实际录制最大时间
     */
    private int mMaxTime;
    /**
     * 视频实际录制最小时间
     */
    private int mMinTime;
    /**
     * 外圆半径
     */
    private float mExCircleRadius, mInitExCircleRadius;
    /**
     * 内圆半径
     */
    private float mInnerCircleRadius, mInitInnerRadius;
    /**
     * 外圆颜色
     */
    private int mAnnulusColor;
    /**
     * 内圆颜色
     */
    private int mInnerCircleColor;
    /**
     * 进度条颜色
     */
    private int mProgressColor;
    /**
     * 外圆画笔
     */
    private Paint mExCirclePaint;
    /**
     * 内圆画笔
     */
    private Paint mInnerCirclePaint;
    /**
     * 进度条画笔
     */
    private Paint mProgressPaint;

    /**
     * 是否正在录制
     */
    private boolean isRecording = false;
    /**
     * 进度条值动画
     */
    private ValueAnimator mProgressAni;

    /**
     * 开始录制时间
     */
    private long mStartTime = 0;
    /**
     * 录制 结束时间
     */
    private long mEndTime = 0;
    /**
     * 长按最短时间  单位毫秒
     */
    public long LONG_CLICK_MIN_TIME = 800;
    private Context context;
    private int mWidth;
    private int mHeight;
    private float mCurrentProgress;
    private MHandler handler = new MHandler(this);

    public VideoControlView(Context context) {
        this(context, null);
    }


    public VideoControlView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context, attrs);
    }

    /**
     * 获取布局属性
     *
     * @param context
     * @param attrs
     */
    private void initData(Context context, AttributeSet attrs) {
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VideoControlView);
        mMaxTime = a.getInt(R.styleable.VideoControlView_maxTime, VIDEO_RECORD_DEFAULT_MAX_TIME);
        mMinTime = a.getInt(R.styleable.VideoControlView_minTime, VIDEO_RECORD_DEFAULT_MIN_TIME);

        excicleMagnification = a.getFloat(R.styleable.VideoControlView_excicleMagnification
                , EXCICLE_MAGNIFICATION);
        innerCircleShrinks = a.getFloat(R.styleable.VideoControlView_excicleMagnification
                , INNER_CIRCLE_SHRINKS);
        if (excicleMagnification < 1) {
            throw new RuntimeException("外圆放大倍数必须大于1");
        }
        if (innerCircleShrinks > 1) {
            throw new RuntimeException("内圆缩小倍数必须小于1");
        }

        mInitExCircleRadius = mExCircleRadius = a.getDimension(R.styleable.VideoControlView_excircleRadius
                , VIDEO_RECORD_DEFAULT_EXCIRCLE_RADIUS);
        mInitInnerRadius = mInnerCircleRadius = a.getDimension(R.styleable.VideoControlView_innerCircleRadius
                , VIDEO_RECORD_DEFAULT_INNER_CIRCLE_RADIUS);

        mAnnulusColor = a.getColor(R.styleable.VideoControlView_annulusColor
                , VIDEO_RECORD_DEFAULT_ANNULUS_COLOR);
        mInnerCircleColor = a.getColor(R.styleable.VideoControlView_innerCircleColor
                , VIDEO_RECORD_DEFAULT_INNER_CIRCLE_COLOR);
        mProgressColor = a.getColor(R.styleable.VideoControlView_progressColor
                , VIDEO_RECORD_DEFAULT_PROGRESS_COLOR);
        a.recycle();
        //初始化外圆画笔
        mExCirclePaint = new Paint();
        mExCirclePaint.setColor(mAnnulusColor);

        //初始化内圆画笔
        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setColor(mInnerCircleColor);

        //初始化进度条画笔
        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeWidth(mExCircleRadius - mInnerCircleRadius);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        //进度条的属性动画
        mProgressAni = ValueAnimator.ofFloat(0, 360f);
        mProgressAni.setDuration(mMaxTime * 1000);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (mExCircleRadius * 2 * excicleMagnification > Math.min(mWidth, mHeight)) {
            throw new RuntimeException("设置的半径的2 * " + excicleMagnification + "倍要小于宽和高中的最小值的");
        }
        if (mInnerCircleRadius > mExCircleRadius) {
            throw new RuntimeException("设置的内圆半径要小于外圆半径");
        } else if (mInnerCircleRadius == mExCircleRadius) {
            Log.e(TAG, "mInnerCircleRadius == mExCircleRadius 你将看不到进度条");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画外圆
        canvas.drawCircle(mWidth / 2, mHeight / 2, mExCircleRadius, mExCirclePaint);
        //画内圆
        canvas.drawCircle(mWidth / 2, mHeight / 2, mInnerCircleRadius, mInnerCirclePaint);
        if (isRecording) {
            drawProgress(canvas);
        }
    }

    /**
     * 绘制圆形进度条
     * Draw a circular progress bar.
     *
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        final RectF rectF = new RectF(
                mWidth / 2 - (mInnerCircleRadius + (mExCircleRadius - mInnerCircleRadius) / 2),
                mHeight / 2 - (mInnerCircleRadius + (mExCircleRadius - mInnerCircleRadius) / 2),
                mWidth / 2 + (mInnerCircleRadius + (mExCircleRadius - mInnerCircleRadius) / 2),
                mHeight / 2 + (mInnerCircleRadius + (mExCircleRadius - mInnerCircleRadius) / 2));
        canvas.drawArc(rectF, -90, mCurrentProgress, false, mProgressPaint);
    }

    @Override
    public boolean performClick() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isRecording = true;
                mStartTime = System.currentTimeMillis();
                handler.sendEmptyMessageDelayed(MSG_START_LONG_RECORD, LONG_CLICK_MIN_TIME);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isRecording = false;
                mEndTime = System.currentTimeMillis();
                if (mEndTime - mStartTime < LONG_CLICK_MIN_TIME) {
                    //Long press the action time too short.
                    if (handler.hasMessages(MSG_START_LONG_RECORD)) {
                        handler.removeMessages(MSG_START_LONG_RECORD);
                    }
                    if (onRecordListener != null) {
                        onRecordListener.onShortClick();
                    }

                } else {

                    if (mProgressAni != null && mProgressAni.getCurrentPlayTime() / 1000 < mMinTime) {
                        //The recording time is less than the minimum recording time.
                        if (onRecordListener != null) {
                            onRecordListener.OnFinish(0);
                        }
                    } else {
                        //The end of the normal
                        if (onRecordListener != null) {
                            onRecordListener.OnFinish(1);
                        }
                    }
                }
                mExCircleRadius = mInitExCircleRadius;
                mInnerCircleRadius = mInitInnerRadius;
                mProgressAni.cancel();
                startAnimation(
                        mInitExCircleRadius * excicleMagnification,
                        mInitExCircleRadius,
                        mInitInnerRadius * innerCircleShrinks,
                        mInitInnerRadius);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    /**
     * 设置外圆 内圆缩放动画
     *
     * @param bigStart
     * @param bigEnd
     * @param smallStart
     * @param smallEnd
     */
    private void startAnimation(float bigStart, float bigEnd, float smallStart, float smallEnd) {
        ValueAnimator bigObjAni = ValueAnimator.ofFloat(bigStart, bigEnd);
        bigObjAni.setDuration(150);
        bigObjAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mExCircleRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        ValueAnimator smallObjAni = ValueAnimator.ofFloat(smallStart, smallEnd);
        smallObjAni.setDuration(150);
        smallObjAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerCircleRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        bigObjAni.start();
        smallObjAni.start();

        smallObjAni.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //开始绘制圆形进度
                if (isRecording) {
                    startAniProgress();
                }

            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    /**
     * 开始圆形进度值动画
     */
    private void startAniProgress() {
        if (mProgressAni == null) {
            return;
        }
        mProgressAni.start();
        mProgressAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mProgressAni.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                isRecording = false;
                mCurrentProgress = 0;
                invalidate();
            }
        });
    }

    /**
     * 设置最大录制时间
     *
     * @param mMaxTime 最大录制时间 秒
     */
    public void setMaxTime(int mMaxTime) {
        this.mMaxTime = mMaxTime;
    }

    /**
     * 设置最小录制时间
     *
     * @param mMinTime 最小录制时间 秒
     */
    public void setMinTime(int mMinTime) {
        this.mMinTime = mMinTime;
    }

    /**
     * 设置外圆半径
     *
     * @param mExCircleRadius 外圆半径
     */
    public void setExCircleRadius(float mExCircleRadius) {
        this.mExCircleRadius = mExCircleRadius;
    }

    /**
     * 设置内圆半径
     *
     * @param mInnerCircleRadius 内圆半径
     */
    public void setInnerCircleRadius(float mInnerCircleRadius) {
        this.mInnerCircleRadius = mInnerCircleRadius;
    }

    /**
     * 设置颜色外圆颜色
     *
     * @param mAnnulusColor
     */
    public void setAnnulusColor(int mAnnulusColor) {
        this.mAnnulusColor = mAnnulusColor;
        mExCirclePaint.setColor(mAnnulusColor);
    }

    /**
     * 设置进度圆环颜色
     *
     * @param mProgressColor
     */
    public void setProgressColor(int mProgressColor) {
        this.mProgressColor = mProgressColor;
        mProgressPaint.setColor(mProgressColor);
    }

    /**
     * 设置内圆颜色
     *
     * @param mInnerCircleColor
     */
    public void setInnerCircleColor(int mInnerCircleColor) {
        this.mInnerCircleColor = mInnerCircleColor;
        mInnerCirclePaint.setColor(mInnerCircleColor);
    }

    private OnRecordListener onRecordListener;

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public static abstract class OnRecordListener {

        /**
         * 点击拍照
         */
        public void onShortClick() {
        }

        /**
         * 开始录制
         */
        public abstract void OnRecordStartClick();

        /**
         * 录制结束
         *
         * @param resultCode 0 录制时间太短 1 正常结束
         */
        public abstract void OnFinish(int resultCode);
    }

    /**
     * 长按录制
     */
    private static final int MSG_START_LONG_RECORD = 0x1;


    static class MHandler extends android.os.Handler {

        private WeakReference<VideoControlView> weakReference = null;

        public MHandler(VideoControlView controlView) {
            weakReference = new WeakReference<VideoControlView>(controlView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference == null || weakReference.get() == null) return;
            final VideoControlView videoControlView = weakReference.get();
            switch (msg.what) {
                case VideoControlView.MSG_START_LONG_RECORD:
                    if (videoControlView.onRecordListener != null) {
                        videoControlView.onRecordListener.OnRecordStartClick();
                    }
                    //内外圆动画，内圆缩小，外圆放大
                    videoControlView.startAnimation(videoControlView.mExCircleRadius,
                            videoControlView.mExCircleRadius * videoControlView.excicleMagnification,
                            videoControlView.mInnerCircleRadius,
                            videoControlView.mInnerCircleRadius * videoControlView.excicleMagnification);
                    break;
            }
        }
    }
}
