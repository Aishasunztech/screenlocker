package com.screenlocker.secure.settings.Wallpaper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.screenlocker.secure.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class SetWallpaperDialog extends Dialog {
    private OnSetWallpaperListener mListener;

    private int mId = 0;
    private ImageView imgView;

    public interface OnSetWallpaperListener {
        void onWallpaperSelected(int id);
    }

    public SetWallpaperDialog(@NonNull Context context, OnSetWallpaperListener mListener) {
        super(context);
        setContentView(R.layout.wallpaper_dialog);
        this.mListener = mListener;
        imgView = findViewById(R.id.imageView);
        findViewById(R.id.btnConfirm).setOnClickListener(vi -> {
            mListener.onWallpaperSelected(mId);
        });
    }

    public void setImage(int id) {
        mId = id;
        imgView.setImageResource(id);
    }


}
