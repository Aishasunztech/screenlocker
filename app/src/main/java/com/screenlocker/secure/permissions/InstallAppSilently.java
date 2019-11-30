package com.screenlocker.secure.permissions;

import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.secure.launcher.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstallAppSilently extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_app_silently);
    }
//    private void puk(Context context) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//
//        String fileName = PAKAGE_FILE_NAME ;
//        String dir_type = Environment.DIRECTORY_DOWNLOADS;
//
//        File dir= Environment.getExternalStoragePublicDirectory(dir_type);
//        java.io.File file = new java.io.File(dir ,fileName);
//        Uri packageUri = Uri.fromFile(file);
//
//        PackageManager pm = context.getPackageManager();
//
//        Class<? extends PackageManager> o = pm.getClass();
//        Method[] allMethods=o.getMethods();
//
//        for (Method m : allMethods) {
//            if (m.getName().equals("installPackage")) {
//                Timber.e("installing the app..");
//                m.invoke(pm,new Object[] { packageUri, null, 1, "com.mic.zapp"});
//                break;
//            }
//        }
//
//    }

}
