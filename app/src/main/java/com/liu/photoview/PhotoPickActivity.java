package com.liu.photoview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liu.photoview.adapter.PhotoAdapter;
import com.liu.photoview.bean.PhotoFolder;
import com.liu.photoview.util.OtherUtils;
import com.liu.photoview.view.PopuListImage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PhotoPickActivity extends AppCompatActivity implements PhotoAdapter.OnItemClickListener, View.OnClickListener, PopuListImage.OnPopuWindowItemClickListen {

    private static final int    REQUEST_CAMERA = 1;
    private static final String KEY_RESULT     = "phote_select_result";
    //ListView
    private RecyclerView   mPhotoListView;
    //底部容器
    private RelativeLayout mMRlBottom;
    //图片文件夹名称
    private TextView       mTvPathName;
    //文件夹图片数量
    private TextView       mTvPhotoNum;

    //图片
    List<String> mPhotoLists;
    //当前文件夹
    File         mCurrentFile;

    //最大个数
    int mMaxtCount;

    List<PhotoFolder> mPhotoFolders = new ArrayList<PhotoFolder>();

    private ProgressDialog mProgressDialog;

    private PhotoAdapter mPhotoAdapter;

    private static final int DATA_LOADED = 0X110;

    private PopuListImage mPopuListImage;

    /**
     * 相机照料像返回的文件
     */
    private File mTmpFile;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //隐藏进度对话框
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();

                //绑定数据到VIEW中
                data2View();
                //初始化PopupWindow
                initPopupWindow();
            }

        }
    };


    /**
     * 初始化PopupWindow
     */
    private void initPopupWindow() {
        mPopuListImage = new PopuListImage(PhotoPickActivity.this, mPhotoFolders);

        mPopuListImage.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //屏幕变亮
                lightOn();
            }
        });
        mPopuListImage.setOnPopuWindowItemClickListen(this);
    }

    private void lightOn() {

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }


    /**
     * 数据转换
     */
    private void data2View() {


        if (mCurrentFile == null) {
            Toast.makeText(this, "没有扫描到任何力道图片", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取当前文件夹的所有图片
        mPhotoLists = Arrays.asList(mCurrentFile.list());

        initRecyleView(mPhotoLists, mCurrentFile.getAbsolutePath());

        mTvPathName.setText(mCurrentFile.getName());
        mTvPhotoNum.setText(mPhotoLists.size() + "");
    }

    /**
     * 初始化适配器
     *
     * @param photoLists
     */
    private void initRecyleView(List<String> photoLists, String dirPath) {

        if (photoLists == null) {
            Toast.makeText(this, "没有扫描到任何力道图片", Toast.LENGTH_SHORT).show();
            return;
        }

        mPhotoAdapter = new PhotoAdapter(this, photoLists, dirPath);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(PhotoPickActivity.this, 3);
        mPhotoListView.setLayoutManager(gridLayoutManager);
        mPhotoListView.setAdapter(mPhotoAdapter);
        mPhotoAdapter.setmOnItemClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }


    private void initView() {

        mPhotoListView = (RecyclerView) findViewById(R.id.photoListView);


        mMRlBottom = (RelativeLayout) findViewById(R.id.rlBottom);

        mTvPathName = (TextView) findViewById(R.id.tvPathName);
        mTvPhotoNum = (TextView) findViewById(R.id.tvPhotoNum);
    }

    /**
     * 扫描手机中的图片
     */
    private void initData() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //判断外部存储卡是否存在
            Toast.makeText(this, "外部存储卡不可用", Toast.LENGTH_LONG).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this, "请稍后...", "正在加载中.....");


        //开启线程扫描
        new Thread() {
            @Override
            public void run() {

                //获取URI
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                ContentResolver contentResolver = PhotoPickActivity.this.getContentResolver();

                Cursor cursor = contentResolver.query(uri, null,//查询URI，所有列
                        MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ?", //过滤条件 只查jpg,png
                        new String[]{"image/jpeg", "image/png"}, //条件
                        MediaStore.Images.Media.DATE_MODIFIED);//排序修改时间


                //防止重复扫描文件夹
                HashSet<String> mDirPath = new HashSet<String>();
                while (cursor.moveToNext()) {
                    //获取图片路径-列索引
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获取父文件
                    File parentFile = new File(path).getParentFile();
                    //判断是否为NULL
                    if (parentFile == null) {
                        continue;
                    }

                    //获取父文件夹路径
                    String      dirPath     = parentFile.getAbsolutePath();
                    PhotoFolder photoFolder = null;


                    if (mDirPath.contains(dirPath)) {
                        continue;
                    } else {
                        //
                        mDirPath.add(dirPath);

                        photoFolder = new PhotoFolder();
                        photoFolder.setPath(dirPath);

                        photoFolder.setFirstImagePath(path);

                    }

                    if (parentFile.list() == null) {
                        continue;
                    }

                    //文件夹下的图片数量
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {

                            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }).length;

                    photoFolder.setImageCount(picSize);

                    //放在集合中
                    mPhotoFolders.add(photoFolder);


                    if (picSize > mMaxtCount) {
                        mMaxtCount = picSize;
                        //当前文件夹就是当前路径的父文件夹
                        mCurrentFile = parentFile;
                    }


                }

                cursor.close();
                //防止重复扫描
                mDirPath = null;
                //查询完成-发送消息到handler中
                mHandler.sendEmptyMessage(DATA_LOADED);


            }
        }.start();


    }

    private void initListener() {
        mMRlBottom.setOnClickListener(this);


    }

    @Override
    public void onItemClick(int position, View itemView) {


        if (position == 0 && mPhotoAdapter.getIsShowCamera()) {
            showCamera();
            return;
        }

        List<String> selectedImage = mPhotoAdapter.getSelectedImage();

        String path     = mPhotoLists.get(position);
        String filePath = mCurrentFile.getAbsolutePath() + "/" + path;

        if (mPhotoAdapter.getIsRadio()) {

            if(selectedImage.contains(filePath)){

                selectedImage.remove(filePath);
            }else{
                selectedImage.clear();
                selectedImage.add(filePath);

            }


        } else {

            if (selectedImage.contains(filePath)) {

                selectedImage.remove(filePath);

            } else {
                selectedImage.add(filePath);

            }
        }

        Log.d("vivi", "selectedImage.size():" + selectedImage.size());
        mPhotoAdapter.notifyDataSetChanged();

        //        ImageButton ibSelect = (ImageButton) itemView.findViewById(R.id.ibSelect);
        //        ImageView   ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);


        //mPhotoAdapter.notifyDataSetChanged();


    }

    /**
     * 显示相机
     */
    private void showCamera() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = OtherUtils.createFile(getApplicationContext());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getApplicationContext(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rlBottom:
                showPopWindow();
                break;
        }
    }

    private void showPopWindow() {


        //mPopuListImage.setAnimationStyle(R.style.PopuListImageStyle);
        mPopuListImage.showAsDropDown(mMRlBottom);
        //让背景变暗
        lightOff();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    List<String> selectedImage = mPhotoAdapter.getSelectedImage();
                    selectedImage.add(mTmpFile.getAbsolutePath());
                    returnData();

                }
            } else {
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
            }
        }
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putStringArrayListExtra(KEY_RESULT, mPhotoAdapter.getSelectedImage());
        setResult(RESULT_OK, data);
        finish();
    }

    private void lightOff() {

        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.alpha = 0.3f;

        getWindow().setAttributes(lp);


    }

    @Override
    public void onPopuItemClik(int position, View v) {

        PhotoFolder photoFolder = mPhotoFolders.get(position);
        mCurrentFile = new File(photoFolder.getPath());

        String[] list = mCurrentFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        mPhotoLists = Arrays.asList(list);

        mPhotoAdapter.setmDirPath(mCurrentFile.getAbsolutePath());

        mPhotoAdapter.setmDatas(mPhotoLists);

        mPhotoAdapter.notifyDataSetChanged();


        mTvPathName.setText(photoFolder.getName());
        //文件夹图片数量
        mTvPhotoNum.setText(mPhotoLists.size() + "");
        mPopuListImage.dismiss();
    }
}
