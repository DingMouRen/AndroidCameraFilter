package com.dingmouren.camerafilter.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.dingmouren.camerafilter.R;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class DialogFilter extends Dialog {

    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private DialogFilterAdapter mAdapter;
    private OnFilterChangedListener mOnFilterChangedListener;

    public DialogFilter(@NonNull Context context) {
        super(context, R.style.BottomDialog);

        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_filter,null);

        initView(contentView);
        setContentView(contentView);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.windowAnimations = R.style.BottomDialog_Animation;
        getWindow().setAttributes(layoutParams);
        getWindow().setDimAmount(0f);/*使用时设置窗口后面的暗淡量*/
    }

    private void initView(View contentView) {
        mRecycler = contentView.findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(getContext(), OrientationHelper.HORIZONTAL,false);
        mAdapter = new DialogFilterAdapter(getContext());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setAdapter(mAdapter);

        initListener();
    }

    private void initListener() {
        mAdapter.setOnItemClickListener(new DialogFilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                if (null != mOnFilterChangedListener) mOnFilterChangedListener.onFilterChangedListener(position);
                dismiss();
            }
        });
    }

    public void setOnFilterChangedListener(OnFilterChangedListener listener){
        this.mOnFilterChangedListener = listener;
    }

    public interface OnFilterChangedListener{
        void onFilterChangedListener(int position);
    }
}
