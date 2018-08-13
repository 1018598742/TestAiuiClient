package com.iflytek.aiui.demo.chat.model.handler.player;

import android.util.Log;

import com.iflytek.aiui.demo.chat.model.data.SemanticResult;
import com.iflytek.aiui.demo.chat.model.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 故事技能处理类
 */

public class StoryHandler extends IntentHandler {
    public StoryHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
    }

    @Override
    public String getFormatContent() {
        Log.i(LogUtil.TAG, "StoryHandler-getFormatContent: 讲故事格式化数据");
        if (result.data != null) {
            List<AIUIPlayer.SongInfo> songList = new ArrayList<>();
            JSONArray list = result.data.optJSONArray("result");
            if (list != null) {
                for (int index = 0; index < list.length(); index++) {
                    JSONObject audio = list.optJSONObject(index);
                    String audioPath = audio.optString("playUrl");
                    String songName = audio.optString("name");
                    String author = audio.optString("author");

                    songList.add(new AIUIPlayer.SongInfo(author, songName, audioPath));
                }
            }

            if (songList.size() != 0) {
                if (isTTSEnable()) {
                    Log.i(LogUtil.TAG, "StoryHandler-getFormatContent: tts在用");
                    mPlayer.setPreSongList(songList);
                } else {
                    //当语音合成打开时，不能播放
                    Log.i(LogUtil.TAG, "StoryHandler-getFormatContent: tts没在用");
                    mPlayer.playList(songList);
                }
                if (isNeedShowControlTip()) {
                    Log.i(LogUtil.TAG, "StoryHandler-getFormatContent: 需要显示控制条目");
                    result.answer = result.answer + NEWLINE_NO_HTML + NEWLINE_NO_HTML + CONTROL_TIP;
                }
            }
        }
        return result.answer;

    }
}

