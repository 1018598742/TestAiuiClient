package com.iflytek.aiui.demo.chat.ui.chat;

import com.iflytek.aiui.demo.chat.databinding.ChatItemBinding;
import com.iflytek.aiui.demo.chat.model.ChatMessage;

/**
 * 聊天交互内容点击监听
 */
public interface ItemListener {
    public void onMessageClick(ChatMessage msg, ChatItemBinding binding);
}
