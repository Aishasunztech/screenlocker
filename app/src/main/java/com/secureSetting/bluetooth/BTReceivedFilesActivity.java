package com.secureSetting.bluetooth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.secureSetting.bluetooth.BluetoothAdapters.ReceivedFilesAdapter;

import java.io.File;
import java.util.ArrayList;

public class BTReceivedFilesActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView rc;
    private ArrayList<File> namesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SecureAppTheme);
        setContentView(R.layout.activity_btreceived_files);

        toolbar = findViewById(R.id.BTReceivedBar);
        rc = findViewById(R.id.receivedFilesList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bluetooth Received");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        namesList = new ArrayList<>();

        getData();
        rc.setAdapter(new ReceivedFilesAdapter(this,namesList));
        rc.setLayoutManager(new LinearLayoutManager(this));

    }

    private void getData()
    {
        String path = Environment.getExternalStorageDirectory().toString()+"/bluetooth";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null && files.length>0)
        {
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                namesList.add(files[i]);
                Uri fileUri = getImageContentUri(this,files[i]);
                long size = files[i].length()/1024;
//                if(size > 1024)
//                {
//                    size = size/1024;
//                }

                Log.d("Files", "FileName:" + files[i].getName() + " FileSize, " + size );
            }
        }
        else{
            Toast.makeText(this, "empty", Toast.LENGTH_SHORT).show();
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

    private String getSize(Context context, Uri uri) {
        String fileSize = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {

                // get file size
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)) {
                    fileSize = cursor.getString(sizeIndex);
                }
            }
        } finally {
            cursor.close();
        }
        return fileSize;
    }
}
