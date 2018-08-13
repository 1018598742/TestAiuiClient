package com.iflytek.aiui.demo.chat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.iflytek.aiui.demo.chat.model.RawMessage;

/**
 * 聊天消息Room数据库类定义
 */
@Database(entities = {RawMessage.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MessageDB extends RoomDatabase{
    public abstract MessageDao messageDao();
}
