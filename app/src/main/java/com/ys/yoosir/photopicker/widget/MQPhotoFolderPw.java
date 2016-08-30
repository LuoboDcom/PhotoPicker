package com.ys.yoosir.photopicker.widget;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ys.yoosir.photopicker.R;
import com.ys.yoosir.photopicker.imageloader.MQImage;
import com.ys.yoosir.photopicker.model.ImageFolderModel;

import java.util.ArrayList;
import java.util.List;

/** 图片悬着界面中的图片目录选择窗口
 * Created by ys on 2016/8/29.
 */

public class MQPhotoFolderPw extends MQBasePopupWindow implements AdapterView.OnItemClickListener{

    public static final int ANIM_DURATION = 300;
    private Callback mCallback;
    private LinearLayout mRootLl;
    private ListView mContentLv;
    private FolderAdapter mFolderAdapter;
    private int mCurrentPosition;

    public MQPhotoFolderPw(Activity activity, View anchorView, Callback callback) {
        super(activity, R.layout.pw_photo_folder, anchorView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mCallback = callback;
    }

    @Override
    protected void initView() {
        mRootLl = getViewById(R.id.root_ll);
        mContentLv = getViewById(R.id.content_lv);
    }

    @Override
    protected void setListener() {
        mRootLl.setOnClickListener(this);
        mContentLv.setOnItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        setAnimationStyle(android.R.style.Animation);
        setBackgroundDrawable(new ColorDrawable(0x90000000));

        mFolderAdapter= new FolderAdapter();
        mContentLv.setAdapter(mFolderAdapter);
    }

    @Override
    public void show() {
        showAsDropDown(mAnchorView);
        ViewCompat.animate(mContentLv).translationY(-mWindowRootView.getHeight()).setDuration(0).start();
        ViewCompat.animate(mContentLv).translationY(0).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(ANIM_DURATION).start();
    }

    @Override
    public void dismiss() {
        ViewCompat.animate(mContentLv).translationY(-mWindowRootView.getHeight()).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(ANIM_DURATION).start();

        if (mCallback != null) {
            mCallback.executeDismissAnim();
        }

        mContentLv.postDelayed(new Runnable() {
            @Override
            public void run() {
                MQPhotoFolderPw.super.dismiss();
            }
        }, ANIM_DURATION);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.root_ll){
            dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mCallback != null && mCurrentPosition != position){
            mCallback.onSelectedFolder(position);
        }
        mCurrentPosition = position;
        dismiss();
    }

    public int getCurrentPosition(){
        return mCurrentPosition;
    }

    /**
     *  设置目录数据集合
     * @param datas
     */
    public void setDatas(ArrayList<ImageFolderModel> datas){
        mFolderAdapter.setDatas(datas);
    }

    private class FolderAdapter extends BaseAdapter{

        private List<ImageFolderModel> mDatas;
        private int mImageWidth;
        private int mImageHeight;

        public FolderAdapter() {
            mDatas = new ArrayList<>();
            mImageWidth = mActivity.getResources().getDisplayMetrics().widthPixels / 10 ;
            mImageHeight = mImageWidth;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderViewHolder folderViewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_folder,parent,false);
                folderViewHolder = new FolderViewHolder();
                folderViewHolder.photoIv = (YSImageView) convertView.findViewById(R.id.photo_iv);
                folderViewHolder.nameTv = (TextView) convertView.findViewById(R.id.name_tv);
                folderViewHolder.countTv = (TextView) convertView.findViewById(R.id.count_tv);
                convertView.setTag(folderViewHolder);
            }else{
                folderViewHolder = (FolderViewHolder) convertView.getTag();
            }
            ImageFolderModel imageFolderModel = (ImageFolderModel) getItem(position);
            folderViewHolder.nameTv.setText(imageFolderModel.name);
            folderViewHolder.countTv.setText(imageFolderModel.getCount()+"");
            MQImage.displayImage(mActivity, folderViewHolder.photoIv, imageFolderModel.coverPath, R.mipmap.ic_holder_light, R.mipmap.ic_holder_light, mImageWidth, mImageHeight, null);

            return convertView;
        }

        public void setDatas(ArrayList<ImageFolderModel> datas){
            if(datas != null){
                mDatas = datas;
            }else{
                mDatas.clear();
            }
            notifyDataSetChanged();
        }

    }

    private class FolderViewHolder{
        public YSImageView photoIv;
        public TextView nameTv;
        public TextView countTv;
    }

    public interface Callback {
        void onSelectedFolder(int position);

        void executeDismissAnim();
    }
}
