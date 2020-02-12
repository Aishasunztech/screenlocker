package com.contactSupport;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.AppExecutor;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 6/1/2019.
 */
public class ChatViewModel extends AndroidViewModel {

    public ChatViewModel(@NonNull Application application) {
        super(application);

    }
}
