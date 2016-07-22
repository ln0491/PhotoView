package com.liu.photoview.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.liu.photoview.R;
import com.liu.photoview.adapter.PopuAdapter;
import com.liu.photoview.bean.PhotoFolder;

import java.util.List;

/**
 * Created by 刘楠 on 2016/7/21 0021.21:57
 */
public class PopuListImage extends PopupWindow {
    Context mContext;

    int mWidth;
    int mHeight;

    private RecyclerView mRecyclerView;

    private View mConvertView;

    List<PhotoFolder> mDatas;
    private PopuAdapter mPopuAdapter;

    public PopuListImage(Context context,List<PhotoFolder> datas) {
        super(context);
        this.mContext = context;


        caculeWidthAndHeight(context);

        mConvertView = LayoutInflater.from(context).inflate(R.layout.layout_popu,null);
        this.mDatas=datas;



        this.setWidth(mWidth);
        this.setHeight(mHeight);

        this.setContentView(mConvertView);
        //设置焦点
        this.setFocusable(true);
        //可以触摸
        setTouchable(true);
        //设置可以点击外部
        this.setOutsideTouchable(true);

        //设置背景
        this.setBackgroundDrawable(new ColorDrawable());

        this.setAnimationStyle(R.style.PopuListImageStyle);

        //设置点击外部可以消失

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //判断是不是点击了外部
                if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                //不点击外部
                return false;
            }
        });

        initView();
        initData();
    }

    /**
     * 计算宽与高
     * @param context
     */
    private void caculeWidthAndHeight(Context context) {
        //获取WindowsManger
        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取屏幕的宽与高
        DisplayMetrics outMetris = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetris);

        mWidth =outMetris.widthPixels;
        mHeight= (int) (outMetris.heightPixels*0.7);


    }

    /**
     * 初始化
     */
    private void initView() {

        mRecyclerView = (RecyclerView) mConvertView.findViewById(R.id.popRcView);
    }

    /**
     * 数据与Adapter
     */
    private void initData() {
        mPopuAdapter = new PopuAdapter(mContext,mDatas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mPopuAdapter);

        mPopuAdapter.setOnItemClickListener(new PopuAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position, View itemView) {

                mOnPopuWindowItemClickListen.onPopuItemClik(position,itemView);
            }
        });


    }
    OnPopuWindowItemClickListen mOnPopuWindowItemClickListen;

    public void setOnPopuWindowItemClickListen(OnPopuWindowItemClickListen onPopuWindowItemClickListen){
        this.mOnPopuWindowItemClickListen=onPopuWindowItemClickListen;
    }

    public interface  OnPopuWindowItemClickListen{

        void onPopuItemClik(int position,View v);
    }


}
