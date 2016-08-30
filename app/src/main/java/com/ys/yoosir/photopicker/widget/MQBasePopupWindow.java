package com.ys.yoosir.photopicker.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.KeyEvent;
import android.widget.PopupWindow;
import android.view.View;

/** PopupWindow 基类
 * Created by ys on 2016/8/29.
 */

public abstract class MQBasePopupWindow extends PopupWindow implements View.OnClickListener {

    protected  Activity mActivity;
    protected  View mWindowRootView;
    protected  View mAnchorView;

    public MQBasePopupWindow(Activity activity, @LayoutRes int layoutId,View anchorView , int width , int height) {
        super(View.inflate(activity,layoutId,null),width,height,true);
        init(activity,anchorView);

        initView();
        setListener();
        processLogic();
    }

    private void init(Activity activity, View anchorView) {
        getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        //如果想让在点击别的地方的时候，关闭掉弹出窗体，一定要记得给mPopupWindow设置一个背景资源
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.mAnchorView = anchorView;
        mActivity = activity;
        mWindowRootView = activity.getWindow().peekDecorView();
    }

    protected abstract void initView();

    protected abstract void setListener();

    protected abstract void processLogic();

    public abstract void show();

    @Override
    public void onClick(View v) {

    }

    /**
     *  查找 View
     * @param id    id 控件ID
     * @param <VT>  View 类型
     * @return  返回控件对象
     */
    protected  <VT extends View> VT getViewById(@IdRes int id){
        return (VT) getContentView().findViewById(id);
    }
}
