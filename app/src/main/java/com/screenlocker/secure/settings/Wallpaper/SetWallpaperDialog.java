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

public class SetWallpaperDialog extends DialogFragment {
    private OnSetWallpaperListener mListener;
    public interface OnSetWallpaperListener{
        void onWallpaperSelected(int id);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.wallpaper_dialog, container, false);
        ImageView imgView = v.findViewById(R.id.imageView);
        imgView.setImageResource(bundle.getInt("RAWID",0));
        v.findViewById(R.id.btnConfirm).setOnClickListener(vi->{
            mListener.onWallpaperSelected(bundle.getInt("RAWID"));
        });
        return v;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnSetWallpaperListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
