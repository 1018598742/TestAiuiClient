package com.iflytek.aiui.demo.chat.model;

import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

/**
 * 聊天界面交互消息
 */

public class ChatMessage {
    private RawMessage mMsgImpl;
    private ChatMessageHandler mHandler;
    private ChatViewModel mModel;

    public ChatMessage(RawMessage message, PermissionChecker checker, ChatViewModel viewModel, PlayerViewModel player) {
        mModel = viewModel;
        mMsgImpl = message;
        mHandler = new ChatMessageHandler(viewModel, player, checker, message);
    }

    public RawMessage getMessage() {
        return mMsgImpl;
    }

    public ChatMessageHandler getHandler() {
        return mHandler;
    }

    public String getDisplayText() {
        if (mMsgImpl.cacheContent == null) {
            mMsgImpl.cacheContent = mHandler.getFormatMessage();
            mModel.updateMessage(mMsgImpl);
        }

        return mMsgImpl.cacheContent;
    }
}
