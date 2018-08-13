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
 * 音乐技能和操作指令处理处理
 */

public class MusicHandler extends IntentHandler {
    public MusicHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
    }

    @Override
    public String getFormatContent() {
        String intent = result.semantic.optString("intent");
        Log.i(LogUtil.TAG, "MusicHandler-getFormatContent: 音乐内的意图是：" + intent);
        // 播放指令
        if (intent.equals("INSTRUCTION")) {
            JSONArray slots = result.semantic.optJSONArray("slots");
            for (int index = 0; index < slots.length(); index++) {
                JSONObject slot = slots.optJSONObject(index);
                if ("insType".equals(slot.optString("name"))) {
                    String instruction = slot.optString("value");
                    if ("next".equals(instruction)) {
                        mPlayer.next();
                    } else if ("past".equals(instruction)) {
                        mPlayer.prev();
                    } else if ("pause".equals(instruction)) {
                        mPlayer.pause();
                    } else if ("replay".equals(instruction)) {
                        mPlayer.play();
                    }
                }
            }

            return "已完成操作";
        } else {
            if (result.data != null) {
                // 解析音乐结果并播放
                List<AIUIPlayer.SongInfo> songList = new ArrayList<>();
                JSONArray list = result.data.optJSONArray("result");
                if (list != null) {
                    for (int index = 0; index < list.length(); index++) {
                        JSONObject audio = list.optJSONObject(index);
                        String audioPath = audio.optString("audiopath");
                        String songname = audio.optString("songname");
                        String author = audio.optJSONArray("singernames").optString(0);

                        songList.add(new AIUIPlayer.SongInfo(author, songname, audioPath));
                    }

                }
//                songList.add(new AIUIPlayer.SongInfo("测试", "海阔天空", "http://zhangmenshiting.qianqian.com/data2/music/3519cdb70c14a95076e8c006c7226963/599516462/599516462.mp3?xcode=0145419a3ac00f6ff5d44a3a1acfff98"));


                if (songList.size() != 0) {
                    if (isTTSEnable()) {
                        mPlayer.setPreSongList(songList);
                    } else {
                        mPlayer.playList(songList);
                    }
                    if (isNeedShowControlTip()) {
                        result.answer = result.answer + NEWLINE_NO_HTML + NEWLINE_NO_HTML + CONTROL_TIP;
                    }
                }
            }
            return result.answer;
        }

    }
}

