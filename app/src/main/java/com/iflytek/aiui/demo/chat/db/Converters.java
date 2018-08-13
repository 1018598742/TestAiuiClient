package com.iflytek.aiui.demo.chat.db;

import android.arch.persistence.room.TypeConverter;

import com.iflytek.aiui.demo.chat.model.RawMessage;

/**
 * Room 数据库类型转换
 */

public class Converters {
    @TypeConverter
    public int msgTypeConvert(RawMessage.MsgType type){
        return type.ordinal();
    }

    @TypeConverter
    public RawMessage.MsgType msgTypeConvert(int type){
        return RawMessage.MsgType.values()[type];
    }

    @TypeConverter
    public int fromTypeConvert(RawMessage.FromType type){
        return type.ordinal();
    }

    @TypeConverter
    public RawMessage.FromType fromTypeConvert(int type){
        return RawMessage.FromType.values()[type];
    }
}
