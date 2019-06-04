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
    private MyAppDatabase mDB;
     private LiveData<List<ChatMessages>> mMessgaes;
    public ChatViewModel(@NonNull Application application) {
        super(application);
        mDB = MyApplication.getAppDatabase(application);
        mMessgaes =  mDB.getDao().getAllMessages();
    }
    LiveData<List<ChatMessages>> getAllMessages (){
        return mMessgaes;
    }
    void insertMessage(ChatMessages msg){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            mDB.getDao().insertMessage(msg);
        });
    }
    void updateMessage(ChatMessages msg){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            mDB.getDao().updateMessage(msg);
        });
    }
    void deleteMessage(ChatMessages msg){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            mDB.getDao().deleteMessage(msg);
        });
    }
}
