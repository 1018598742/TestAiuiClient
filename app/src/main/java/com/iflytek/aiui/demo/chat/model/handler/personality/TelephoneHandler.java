package com.iflytek.aiui.demo.chat.model.handler.personality;

import com.iflytek.aiui.demo.chat.model.RawMessage;
import com.iflytek.aiui.demo.chat.model.data.DynamicEntityData;
import com.iflytek.aiui.demo.chat.model.data.SemanticResult;
import com.iflytek.aiui.demo.chat.model.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

/**
 * 打电话技能处理类，用户级动态实体示例
 */

public class TelephoneHandler extends IntentHandler {
    public TelephoneHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
    }

    @Override
    public String getFormatContent() {
        //没有找到联系人，提示上传本地联系人
        if(result.answer.contains("没有为您找到")) {
            StringBuilder builder = new StringBuilder();
            builder.append(result.answer);
            builder.append(NEWLINE);
            builder.append(NEWLINE);
            builder.append("<a href=\"upload_contact\">上传本地联系人数据</a>");
            return builder.toString();
        } else {
            return result.answer;
        }
    }

    @Override
    public boolean urlClicked(String url) {
        if ("upload_contact".equals(url)) {
            mPermissionChecker.checkPermission(android.Manifest.permission.READ_CONTACTS, new Runnable() {
                @Override
                public void run() {
                    uploadContacts();
                }
            }, null);
        }
        return super.urlClicked(url);
    }

    private void uploadContacts() {
        // 上传进度消息，后续根据进度进行更新
        final RawMessage progressMsg = mMessageViewModel.fakeAIUIResult(0, "contacts_upload", "上传进度10%");
        Completable
                .complete()
                .observeOn(Schedulers.io())
                .subscribe(new Action() {
            @Override
            public void run() {
                List<String> contacts = mMessageViewModel.getContacts();
                updateProgress(progressMsg,  "40");

                if(contacts == null || contacts.size() == 0){
                    mMessageViewModel.fakeAIUIResult(0, "contacts_upload", "请允许应用请求的联系人读取权限");
                    return;
                }

                StringBuilder contactJson = new StringBuilder();
                for (String contact : contacts) {
                    String[] nameNumber = contact.split("\\$\\$");
                    contactJson.append(String.format("{\"name\": \"%s\", \"phoneNumber\": \"%s\" }\n",
                            nameNumber[0], nameNumber[1]));
                }
                updateProgress(progressMsg, "70");

                mMessageViewModel.syncDynamicData(new DynamicEntityData(
                        "IFLYTEK.telephone_contact", "uid", "", contactJson.toString()));
                mMessageViewModel.putPersParam("uid", "");
                updateProgress(progressMsg, "100");
            }
        });
    }

    // 更新上传进度信息
    private void updateProgress(RawMessage progressMsg, String progress) {
        progressMsg.msgData = new String(progressMsg.msgData).replaceAll("\\d+", progress).getBytes();
        mMessageViewModel.updateMessage(progressMsg);
    }
}
