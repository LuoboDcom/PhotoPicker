package com.ys.yoosir.photopicker.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**  相机管理类
 * Created by ys on 2016/8/26 0026.
 */
public class YSImageCaptureManager {

    private final static String CAPTURED_PHOTO_PATH_KEY = "CAPTURED_PHOTO_PATH_KEY";
    private String mCurrentPhotoPath;
    private Context mContext;
    private File mImageDir;

    /**
     * @param context   上下文
     * @param imageDir  拍照后图片保存的目录
     */
    public YSImageCaptureManager(Context context,File imageDir){
        mContext = context;
        mImageDir = imageDir;
    }

    /**
     *  创建 拍照图片文件
     * @return 图片文件
     * @throws IOException
     */
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName,".jpg",mImageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     *  获取拍照意图
     * @return  返回意图
     * @throws IOException
     */
    public Intent getTakePictureIntent() throws IOException{
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(mContext.getPackageManager()) != null){
            File photoFile = createImageFile();
            if(photoFile != null){
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
        }
        return takePictureIntent;
    }

    /**
     *  刷新图库
     */
    public void refreshGallery(){
        if(!TextUtils.isEmpty(mCurrentPhotoPath)){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(new File(mCurrentPhotoPath)));
            mContext.sendBroadcast(mediaScanIntent);
            mCurrentPhotoPath = null;
        }
    }

    /**
     *  删除拍摄的照片
     */
    public void deletePhotoFile(){
        if(!TextUtils.isEmpty(mCurrentPhotoPath)){
            try{
                File photoFile = new File(mCurrentPhotoPath);
                photoFile.delete();
                mCurrentPhotoPath = null;
            }catch (Exception e){

            }
        }
    }

    /**
     *  返回当前拍摄的照片路径
     * @return String
     */
    public String getCurrentPhotoPath(){
        return mCurrentPhotoPath;
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        if(savedInstanceState != null && mCurrentPhotoPath != null){
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY,mCurrentPhotoPath);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        if(savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)){
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }
}
