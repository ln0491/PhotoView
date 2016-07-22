package com.liu.photoview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.liu.photoview.R;
import com.liu.photoview.util.OtherUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 刘楠 on 2016/7/20 0020.21:04
 */
public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_PHOTO = 1;
    Context mContext;

    List<String> mDatas;

    String mDirPath;
    /**
    是否显示相机，默认显示
     */
    private boolean mIsShowCAMERA =true;

    /**
     * 是否为单选，默认为多选
     */
    private boolean mIsRadio=false;
    /**
     * 选中的照片保存在集合里，单选与多选共用，在点击是判断是单选还是多选
     */
    private ArrayList<String> mSelectedImage = new ArrayList<>();



    LayoutInflater mInflater;

    public void setIsRadio(boolean radio){
        this.mIsRadio=radio;
    }

    public boolean getIsRadio(){
        return  this.mIsRadio;
    }



    public void setIsShowCamera(boolean showCAMERA){
        this.mIsShowCAMERA=showCAMERA;
    }

    public boolean getIsShowCamera(){
        return this.mIsShowCAMERA;
    }



    public void setmDirPath(String dirPath){
        this.mDirPath=dirPath;
    }

    public void setmDatas(List<String> datas){
        this.mDatas=datas;
    }

    public ArrayList<String> getSelectedImage(){
        return this.mSelectedImage;
    }

    public PhotoAdapter(Context context, List<String> photoLists, String dirPath) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = photoLists;
        this.mDirPath = dirPath;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView=null;
        if(viewType==TYPE_CAMERA&& mIsShowCAMERA){
            itemView = mInflater.inflate(R.layout.item_camera_layout, parent, false);
            return new CaremViewHolder(itemView);
        }else {
            itemView = mInflater.inflate(R.layout.item_photo_list, parent, false);
            return new ViewHolder(itemView);
        }




    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        int itemViewType = getItemViewType(position);
        if(itemViewType==TYPE_CAMERA){
            return ;
        }

        //获取图片的名称
        String path = mDatas.get(position);

        /**
         * 已经选择过的图片，显示出选择过的效果
         */
        PhotoAdapter.ViewHolder myViewHolder = (PhotoAdapter.ViewHolder) holder;
        String filePath  = mDirPath+"/"+path;
        if(!mSelectedImage.contains(filePath)){
            myViewHolder.mIbSelect.setImageResource(R.drawable.picture_unselected);
            myViewHolder.mIvPhoto.setColorFilter(null);
        }else{
            myViewHolder.mIbSelect.setImageResource(R.drawable.pictures_selected);
            myViewHolder.mIvPhoto.setColorFilter(Color.parseColor("#77000000"));
        }
        Picasso.with(mContext).load(new File(mDirPath + "/" + path)).error(R.drawable.pictures_no).placeholder(R.drawable.pictures_no).centerCrop().resize(OtherUtils.dip2px(mContext, 120), OtherUtils.dip2px(mContext, 100)).into(myViewHolder.mIvPhoto);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mDatas.size() > 0) {
            count = mDatas.size();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0&& mIsShowCAMERA){
            return TYPE_CAMERA;
        } else {
            return TYPE_PHOTO;
        }
    }



    public class CaremViewHolder extends  RecyclerView.ViewHolder{

        public CaremViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(getAdapterPosition(), v);
                    }
                }
            });

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView   mIvPhoto;
        ImageButton mIbSelect;


        public ViewHolder(final View itemView) {
            super(itemView);
            mIvPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            mIbSelect = (ImageButton) itemView.findViewById(R.id.ibSelect);

            mIvPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(getAdapterPosition(), itemView);
                    }
                }
            });

        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        void onItemClick(int position, View itemView);
    }
}
