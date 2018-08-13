package com.iflytek.aiui.demo.chat.model.handler.player;

import com.iflytek.aiui.demo.chat.model.data.SemanticResult;
import com.iflytek.aiui.demo.chat.model.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 广播技能处理类
 */

public class RadioDisposer extends IntentHandler {
    public RadioDisposer(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
    }

    @Override
    public String getFormatContent() {
        if(result.data != null) {
            List<AIUIPlayer.SongInfo> songList = new ArrayList<>();
            JSONArray list = result.data.optJSONArray("result");
            if(list != null){
                for(int index = 0; index < list.length(); index++){
                    JSONObject item = list.optJSONObject(index);
                    String audioPath = item.optString("url");
                    String songName = item.optString("name");

                    songList.add(new AIUIPlayer.SongInfo(songName, songName, audioPath));
                }
            }

            if(songList.size() != 0) {
                if(isTTSEnable()){
                    mPlayer.setPreSongList(songList);
                }else {
                    mPlayer.playList(songList);
                }
                if(isNeedShowControlTip()) {
                    result.answer = result.answer + NEWLINE_NO_HTML + NEWLINE_NO_HTML + CONTROL_TIP;
                }
            }

        }
        return result.answer;

    }
}

