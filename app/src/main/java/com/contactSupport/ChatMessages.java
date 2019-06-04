package com.contactSupport;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

/**
 * @author Muhammad Nadeem
 * @Date 6/1/2019.
 */
@Entity(tableName = "messages")
public class ChatMessages implements IMessage {
    @PrimaryKey(autoGenerate = true)
    private long mId;
    private String mText;
    private String mUserId;
    private Date mDate;
    private String mUri;

    @Ignore
    public ChatMessages(String mText, String mUserId, Date mDate, String mUri) {
        this.mText = mText;
        this.mUserId = mUserId;
        this.mDate = mDate;
        this.mUri = mUri;
    }

    public ChatMessages(long mId, String mText, String mUserId, Date mDate, String mUri) {
        this.mId = mId;
        this.mText = mText;
        this.mUserId = mUserId;
        this.mDate = mDate;
        this.mUri = mUri;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public String getUserId() {
        return mUserId;
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getUri() {
        return mUri;
    }
}
