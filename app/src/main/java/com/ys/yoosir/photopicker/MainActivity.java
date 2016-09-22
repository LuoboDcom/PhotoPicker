package com.ys.yoosir.photopicker;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File dirFile = Environment.getExternalStorageDirectory();
        File imageDir = new File(dirFile,"PhotoPicker");
        if(!imageDir.exists()){
            imageDir.mkdirs();
        }
        Intent intent = PhotoPickerActivity.newIntent(this,imageDir,6,new ArrayList<String>(),"完成");
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_PICK_PHOTO){
            if(resultCode == RESULT_OK){
                ArrayList<String> selectedPhoto =  PhotoPickerActivity.getSelectedImages(data);
                for (int i = 0; selectedPhoto != null && i < selectedPhoto.size(); i++) {
                    Log.i("MainActivity",selectedPhoto.get(i));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
