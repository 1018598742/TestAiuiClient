package com.iflytek.aiui.demo.chat.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iflytek.aiui.demo.chat.model.RawMessage;

import java.util.List;

/**
 * 聊天消息操作
 */
@Dao
public interface MessageDao {
    @Insert
    void addMessage(RawMessage msg);

    @Update
    void updateMessage(RawMessage msg);

    @Query("select * from RawMessage")
    LiveData<List<RawMessage>> getAllMessage();
}
