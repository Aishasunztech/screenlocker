package com.secureSetting.bluetooth.BluetoothAdapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.screenlocker.secure.R;

import java.io.File;
import java.util.ArrayList;

public class ReceivedFilesAdapter extends RecyclerView.Adapter<ReceivedFilesAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<File> filesList;

    public ReceivedFilesAdapter(Context context, ArrayList<File> filesList) {
        this.context = context;
        this.filesList = filesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_received_files,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final File file = filesList.get(i);
        String fileName = file.getName();
            myViewHolder.textView.setText(filesList.get(i).getName());
            final String extension = fileName.substring(fileName.lastIndexOf("."));
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(extension.contains("jpg") || extension.contains("png") || extension.contains("gif"))
                    {
                        Intent intentquick = new Intent(Intent.ACTION_VIEW);
                        intentquick.setDataAndType(getImageContentUri(context,file),"image/*");

                        intentquick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentquick);
                    }
                    else if(extension.contains("mp4") || extension.contains("mpeg") || extension.contains("webm"))
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(file.getAbsolutePath()));
                        intent.setData(Uri.parse(file.getAbsolutePath()));
                        intent.setType("video/*");

                        context.startActivity(intent);

//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getAbsolutePath()));
//                        intent.setData(Uri.parse(file.getAbsolutePath()));
//                        intent.setType("video/*");
//                        context.startActivity(intent);

                    }
                }
            });

    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.received_file_name);
        }
    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
    private Uri getVideoContentUri(Context context, File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID },
                MediaStore.Video.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/videos/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (videoFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
