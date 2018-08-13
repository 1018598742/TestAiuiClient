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
 * 笑话播放处理类
 */

public class JokeHandler extends IntentHandler {
    public JokeHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker, SemanticResult result) {
        super(model, player, checker, result);
    }

    @Override
    public String getFormatContent() {
        Log.i(LogUtil.TAG, "JokeHandler-getFormatContent: 获取笑话播放处理类的格式化内容");
        if(result.data != null) {
            Log.i(LogUtil.TAG, "JokeHandler-getFormatContent: 讲笑话数据不为空");
            List<AIUIPlayer.SongInfo> songList = new ArrayList<>();
            JSONArray list = result.data.optJSONArray("result");
            if(list != null){
                for(int index = 0; index < list.length(); index++){
                    JSONObject item = list.optJSONObject(index);
                    // 音频笑话
                    if("1".equals(item.optString("type"))) {
                        String audioPath = item.optString("mp3Url");
                        String songName = item.optString("title");
                        String author = item.optString("author");

                        songList.add(new AIUIPlayer.SongInfo(author, songName, audioPath));
                    } else {
                       // 文本笑话(显示第一条结果)
                        result.answer += NEWLINE_NO_HTML + NEWLINE_NO_HTML + item.optString("content");
                        break;
                    }
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

