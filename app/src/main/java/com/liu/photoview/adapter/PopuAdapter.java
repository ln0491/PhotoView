package com.liu.photoview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.photoview.R;
import com.liu.photoview.bean.PhotoFolder;
import com.liu.photoview.util.OtherUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by 刘楠 on 2016/7/21 0021.22:15
 */
public class PopuAdapter extends RecyclerView.Adapter<PopuAdapter.MyViewHolder> {

    Context mContext;
    List<PhotoFolder> mDatas;

    LayoutInflater mInflater;

    OnItemClickListener mOnItemClickListener;


    public PopuAdapter(Context context, List<PhotoFolder> datas){
        this.mContext=context;
        this.mInflater=LayoutInflater.from(context);
        this.mDatas=datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View convertView = mInflater.inflate(R.layout.item_popu_recyview,parent,false);

        return new MyViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PhotoFolder photoFolder = mDatas.get(position);


        holder.mTvDirName.setText(photoFolder.getName());

        holder.mTvItemCount.setText(photoFolder.getImageCount()+"");



        Picasso.with(mContext)
                .load(new File(photoFolder.getFirstImagePath()))
                .error(R.drawable.pictures_no)
                .placeholder(R.drawable.pictures_no)
                .centerCrop()
                .resize(OtherUtils.dip2px(mContext, 100), OtherUtils.dip2px(mContext, 100))
                .into(holder.mIvItemIcon);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mIvItemIcon;

        TextView mTvDirName;
        TextView mTvItemCount;

        public MyViewHolder(View itemView) {
            super(itemView);
            initView(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemClickListener!=null){
                        mOnItemClickListener.itemClick(getAdapterPosition(),v);
                    }
                }
            });
        }

        private void initView(View itemView) {
            mIvItemIcon = (ImageView) itemView.findViewById(R.id.ivItemIcon);
            mTvDirName = (TextView) itemView.findViewById(R.id.tvDirName);
            mTvItemCount = (TextView) itemView.findViewById(R.id.tvItemCount);
        }
    }

    public interface  OnItemClickListener{
        void itemClick(int position,View itemView);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener=onItemClickListener;
    }
}
