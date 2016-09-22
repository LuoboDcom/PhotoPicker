package com.ys.yoosir.photopicker.model;

import java.util.ArrayList;

/**
 *  图片目录实体类
 * Created by ys on 2016/8/26 0026.
 */
public class ImageFolderModel {

    public String name;
    public String coverPath;
    private ArrayList<String> mImages = new ArrayList<>();
    private boolean mTakePotoEnabled;

    public ImageFolderModel(boolean takePhotoEnabled){
        mTakePotoEnabled = takePhotoEnabled;
        if(takePhotoEnabled){
            //拍照
            mImages.add("");
        }
    }

    public ImageFolderModel(String name, String coverPath) {
        this.name = name;
        this.coverPath = coverPath;
    }

    public boolean isTakePhotoEnabled() {
        return mTakePotoEnabled;
    }

    public void addLastImage(String imagePath){
        mImages.add(imagePath);
    }

    public ArrayList<String> getImages(){
        return mImages;
    }

    public int getCount(){
        return mImages.size();
    }
}
