package com.ys.yoosir.photopicker;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.yoosir.photopicker.imageloader.MQImage;
import com.ys.yoosir.photopicker.model.ImageFolderModel;
import com.ys.yoosir.photopicker.util.MQAsyncTask;
import com.ys.yoosir.photopicker.util.MQLoadPhotoTask;
import com.ys.yoosir.photopicker.util.YSImageCaptureManager;
import com.ys.yoosir.photopicker.widget.MQPhotoFolderPw;
import com.ys.yoosir.photopicker.widget.YSImageView;

import java.io.File;
import java.util.ArrayList;

/**
 *  相册图片选择器
 */
public class PhotoPickerActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener,MQAsyncTask.Callback<ArrayList<ImageFolderModel>>{

    //拍照的请求码
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    //预览照片的请求码
    private static final int REQUEST_CODE_PREVIEW = 2;

    private ImageFolderModel mCurrentImageFolderModel;

    /**
     *  是否可以拍照
     */
    private boolean mTakePhotoEnabled;

    /**
     *  最多选择多少张图片，默认等于1，为单选
     */
    private int mMaxChooseCount = 1;

    /**
     *  右上角按钮文本
     */
    private String mTopRightBtnText;

    /**
     *  图片目录数据集合
     */
    private ArrayList<ImageFolderModel> mImageFolderModels;

    private MQPhotoFolderPw mPhotoFolderPw;

    private int displayWidth;

    /**
     *  上一次显示图片目录的时间戳，防止短时间内重复点击图片目录菜单时界面错乱
     */
    private long mLastShowPhotoFolderTime;
    private MQLoadPhotoTask mLoadPhotoTask;
    private Dialog mLoadingDialog;


    public static Intent newIntent(Context context, File imageDir, int maxChooseCount, ArrayList<String> selectedImages, String topRightBtnText){
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(EXTRA_IMAGE_DIR, imageDir);
        intent.putExtra(EXTRA_MAX_CHOOSE_COUNT, maxChooseCount);
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putExtra(EXTRA_TOP_RIGHT_BTN_TEXT, topRightBtnText);
        return intent;
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if(mTakePhotoEnabled){
            mImageCaptureManager.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        if(mTakePhotoEnabled){
            mImageCaptureManager.onRestoreInstanceState(savedInstanceState);
        }
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO 显示加载等待框
        showLoadingDialog();
        mLoadPhotoTask = new MQLoadPhotoTask(this,this,mTakePhotoEnabled).perform();

    }

    //加载 loading 效果
    private void showLoadingDialog() {
        if(mLoadingDialog == null){
            mLoadingDialog = new Dialog(this,R.style.MQDialog);
            mLoadingDialog.setContentView(R.layout.dialog_loading_photopicker);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog(){
        if(mLoadingDialog != null && mLoadingDialog.isShowing()){
            mLoadingDialog.dismiss();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        setContentView(R.layout.activity_photo_picker);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        cancelLoadPhotoTask();
        super.onDestroy();
    }

    private RelativeLayout          titleView;
    private TextView                titleTV;
    private ImageView               arrowIV;
    private TextView                submitTv;
    private GridView                contentGV;

    private void initView() {
        titleView = (RelativeLayout) findViewById(R.id.title_rl);
        titleTV = (TextView) findViewById(R.id.title_tv);
        arrowIV = (ImageView) findViewById(R.id.arrow_iv);
        submitTv = (TextView) findViewById(R.id.submit_tv);
        contentGV = (GridView) findViewById(R.id.content_gv);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        findViewById(R.id.folder_ll).setOnClickListener(this);
        submitTv.setOnClickListener(this);
        contentGV.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_iv:
                onBackPressed();
                break;
            case R.id.folder_ll:
                if(System.currentTimeMillis() - mLastShowPhotoFolderTime > MQPhotoFolderPw.ANIM_DURATION){
                    showPhotoFolderPw();
                    mLastShowPhotoFolderTime = System.currentTimeMillis();
                }
                break;
            case R.id.submit_tv:
                returnSelectedImages(mPicturePickerAdapter.getSelectedImages());
                break;
        }
    }

    /**
     *  返回已选中的图片集合
     *
     * @param selectedImages  被选中的图片集合
     */
    private void returnSelectedImages(ArrayList<String> selectedImages) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES,selectedImages);
        setResult(RESULT_OK,intent);
        finish();
    }

    /**
     *  显示图集目录界面
     */
    private void showPhotoFolderPw() {
        if(mPhotoFolderPw == null){
            mPhotoFolderPw = new MQPhotoFolderPw(this, titleView, new MQPhotoFolderPw.Callback() {
                @Override
                public void onSelectedFolder(int position) {
                    reloadPhotos(position);
                }

                @Override
                public void executeDismissAnim() {
                    ViewCompat.animate(arrowIV)
                            .setDuration(MQPhotoFolderPw.ANIM_DURATION)
                            .rotation(0)
                            .start();
                }
            });
        }
        mPhotoFolderPw.setDatas(mImageFolderModels);
        mPhotoFolderPw.show();

        ViewCompat.animate(arrowIV).setDuration(MQPhotoFolderPw.ANIM_DURATION).rotation(-180).start();
    }

    /**
     *  重新加载图片
     * @param position 当前目录index
     */
    private void reloadPhotos(int position) {
        if(position < mImageFolderModels.size()){
            mCurrentImageFolderModel = mImageFolderModels.get(position);
            titleTV.setText(mCurrentImageFolderModel.name);
            mPicturePickerAdapter.setData(mCurrentImageFolderModel.getImages());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if(mMaxChooseCount == 1){
            //单选
            if(mCurrentImageFolderModel.isTakePotoEnabled() && position == 0){
                takePhoto();
            }else{
                changeToPreview(position);
            }
        }else{
            //多选
            if(mCurrentImageFolderModel.isTakePotoEnabled() && position == 0){
                if(mPicturePickerAdapter.getSelectedCount() == mMaxChooseCount){
                    toastMaxCountTip();
                }else{
                    takePhoto();
                }
            }else{
                changeToPreview(position);
            }
        }
    }

    private static final String EXTRA_IMAGE_DIR = "EXTRA_IMAGE_DIR";
    private static final String EXTRA_MAX_CHOOSE_COUNT = "EXTRA_MAX_CHOOSE_COUNT";
    private static final String EXTRA_TOP_RIGHT_BTN_TEXT = "EXTRA_TOP_RIGHT_BTN_TEXT";
    private static final String EXTRA_SELECTED_IMAGES = "EXTRA_SELECTED_IMAGES";

    private YSImageCaptureManager mImageCaptureManager;
    private PicturePickerAdapter mPicturePickerAdapter;

    private void processLogic(Bundle savedInstanceState) {
        //获取拍照图片保存目录
        File imageDir = (File) getIntent().getSerializableExtra(EXTRA_IMAGE_DIR);
        if(imageDir != null){
            mTakePhotoEnabled = true;
            mImageCaptureManager = new YSImageCaptureManager(this,imageDir);
        }
        //获取图片选择的最大张数
        mMaxChooseCount = getIntent().getIntExtra(EXTRA_MAX_CHOOSE_COUNT,1);
        if(mMaxChooseCount < 1){
            mMaxChooseCount = 1;
        }

        //获取右上角按钮文本
        mTopRightBtnText = getIntent().getStringExtra(EXTRA_TOP_RIGHT_BTN_TEXT);

        //适配器
        mPicturePickerAdapter = new PicturePickerAdapter(displayWidth);
        mPicturePickerAdapter.setSelectedImages(getIntent().getStringArrayListExtra(EXTRA_SELECTED_IMAGES));
        contentGV.setAdapter(mPicturePickerAdapter);

        renderTopRightBtn();

        titleTV.setText(R.string.all_image);
    }


    /**
     *  拍照
     */
    private void takePhoto(){
        try{
            startActivityForResult(mImageCaptureManager.getTakePictureIntent(),REQUEST_CODE_TAKE_PHOTO);
        }catch (Exception e){
            Toast.makeText(PhotoPickerActivity.this,"此设备不支持拍照",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  跳转到图片选择预览图片
     * @param position  当前点击的item的索引位置
     */
    private void changeToPreview(int position){
//        int currentPosition = position;
//        if(mCurrentImageFolderModel.isTakePotoEnabled()){
//            currentPosition--;
//        }
//        startActivityForResult();
    }

    /**
     *  显示提示信息
     */
    private void toastMaxCountTip(){
        Toast.makeText(PhotoPickerActivity.this,getString(R.string.toast_photo_picker_max,mMaxChooseCount),Toast.LENGTH_SHORT).show();
    }

    /**
     *   刷新提交按钮
     */
    private void renderTopRightBtn(){
        if(mPicturePickerAdapter.getSelectedCount() == 0){
            submitTv.setEnabled(false);
            submitTv.setText(mTopRightBtnText);
        }else{
            submitTv.setEnabled(true);
            submitTv.setText(mTopRightBtnText + "(" + mPicturePickerAdapter.getSelectedCount() + "/" + mMaxChooseCount + ")");
        }
    }

    /**
     *  获取本地图片目录完成
     * @param imageFolderModels 图片目录集合
     */
    @Override
    public void onPostExecute(ArrayList<ImageFolderModel> imageFolderModels) {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
        mImageFolderModels = imageFolderModels;
        reloadPhotos(mPhotoFolderPw == null ? 0 : mPhotoFolderPw.getCurrentPosition());
    }

    /**
     *  已取消加载本地相册回调
     */
    @Override
    public void onTaskCancelled() {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
    }

    /**
     *  取消加载本地相册任务
     */
    private void cancelLoadPhotoTask(){
        if(mLoadPhotoTask != null){
            mLoadPhotoTask.cancelTask();
            mLoadPhotoTask = null;
        }
    }


    private class PicturePickerAdapter extends BaseAdapter {

        private ArrayList<String> mSelectedImages = new ArrayList<>();
        private ArrayList<String> mDatas;
        private int mImageWidth;
        private int mImageHeight;

        public PicturePickerAdapter(int displayWidth){
            mDatas = new ArrayList<>();
            mImageWidth = displayWidth / 10;
            mImageHeight = mImageWidth;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return mDatas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            PicViewHolder picViewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_square_image,parent,false);
                picViewHolder = new PicViewHolder();
                picViewHolder.photoIV = (YSImageView) convertView.findViewById(R.id.photo_iv);
                picViewHolder.flagIV = (ImageView) convertView.findViewById(R.id.flag_iv);
                picViewHolder.tipTV = (TextView) convertView.findViewById(R.id.tip_tv);
                convertView.setTag(picViewHolder);
            }else{
                picViewHolder = (PicViewHolder) convertView.getTag();
            }

            String imagePath = (String) getItem(position);
            if(mCurrentImageFolderModel.isTakePotoEnabled() && position == 0){
                picViewHolder.tipTV .setVisibility(View.VISIBLE);
                picViewHolder.photoIV.setScaleType(ImageView.ScaleType.CENTER);
                picViewHolder.photoIV.setImageResource(R.mipmap.ic_gallery_camera);
                picViewHolder.flagIV.setVisibility(View.INVISIBLE);
                picViewHolder.photoIV.setColorFilter(null);
            }else{
                picViewHolder.tipTV.setVisibility(View.INVISIBLE);
                picViewHolder.photoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                MQImage.displayImage(PhotoPickerActivity.this, picViewHolder.photoIV, imagePath, R.mipmap.ic_holder_dark, R.mipmap.ic_holder_dark, mImageWidth, mImageHeight, null);

                picViewHolder.flagIV.setVisibility(View.VISIBLE);

                if(mSelectedImages.contains(imagePath)){
                    picViewHolder.flagIV.setImageResource(R.mipmap.ic_cb_checked);
                    picViewHolder.photoIV.setColorFilter(getResources().getColor(R.color.photo_selected_color));
                }else{
                    picViewHolder.flagIV.setImageResource(R.mipmap.ic_cb_normal);
                    picViewHolder.photoIV.setColorFilter(null);
                }

                setFlagClickListener(picViewHolder.flagIV,position);
            }

            return convertView;
        }

        private void setFlagClickListener(ImageView flagIV, final int position) {
            flagIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String currentImage = (String) getItem(position);
                    if(mMaxChooseCount == 1){
                        //单选
                        if(getSelectedCount() > 0){
                            String selectedImage = getSelectedImages().remove(0);
                            if(!TextUtils.equals(selectedImage,currentImage)){
                                getSelectedImages().add(currentImage);
                            }
                        }else{
                            getSelectedImages().add(currentImage);
                        }
                        notifyDataSetChanged();
                        renderTopRightBtn();
                    }else{
                        //多选
                        if(!getSelectedImages().contains(currentImage) && getSelectedCount() == mMaxChooseCount){
                            toastMaxCountTip();
                        }else{
                            if(getSelectedImages().contains(currentImage)){
                                getSelectedImages().remove(currentImage);
                            }else{
                                getSelectedImages().add(currentImage);
                            }
                            notifyDataSetChanged();
                            renderTopRightBtn();
                        }
                    }
                }
            });
        }

        public void setData(ArrayList<String> datas){
            if(datas != null){
                mDatas = datas;
            }else{
                mDatas.clear();
            }
            notifyDataSetChanged();
        }

        public ArrayList<String> getData(){
            return mDatas;
        }

        public ArrayList<String> getSelectedImages() {
            return mSelectedImages;
        }

        public void setSelectedImages(ArrayList<String> selectedImages) {
            if (selectedImages != null) {
                mSelectedImages = selectedImages;
            }
            notifyDataSetChanged();
        }

        public int getSelectedCount(){
            return this.mSelectedImages.size();
        }
    }

    private class PicViewHolder{
        public YSImageView photoIV;
        public TextView tipTV;
        public ImageView flagIV;
    }
}
