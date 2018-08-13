package com.iflytek.aiui.demo.chat.model.handler;

import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.model.data.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

/**
 * 拒识（rc = 4）结果处理
 */

public class HintHandler extends IntentHandler {
    private final StringBuilder defaultAnswer;

    public HintHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
        defaultAnswer = new StringBuilder();
        defaultAnswer.append("你好，我不懂你的意思");
        defaultAnswer.append(IntentHandler.NEWLINE_NO_HTML);
        defaultAnswer.append(IntentHandler.NEWLINE_NO_HTML);
        defaultAnswer.append("在后台添加更多技能让我变得更强大吧 :D");
    }

    @Override
    public String getFormatContent() {
        if(TextUtils.isEmpty(result.answer)) {
            return defaultAnswer.toString();
        } else {
            return result.answer;
        }
    }
}
