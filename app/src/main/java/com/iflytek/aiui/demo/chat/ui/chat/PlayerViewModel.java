package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;
import com.iflytek.aiui.demo.chat.repository.player.PlayState;

import java.util.List;

import javax.inject.Inject;

/**
 * 播放器ViewModel
 * 获得播放器状态
 * 控制播放器播放，停止
 */

public class PlayerViewModel extends ViewModel {
    private AIUIPlayer mPlayer;

    @Inject
    public PlayerViewModel(AIUIPlayer player) {
        mPlayer = player;
    }

    public LiveData<PlayState> getPlayState() { return mPlayer.getLiveState(); }

    public void playList(List<AIUIPlayer.SongInfo> list) { mPlayer.playList(list); }

    public void setPreSongList(List<AIUIPlayer.SongInfo> list) { mPlayer.setPreSongList(list); }

    public void playPreSongList() { mPlayer.playPreSongList(); }

    public boolean play() {
        mPlayer.play();
        return true;
    }

    public boolean pause() {
        mPlayer.pause();
        return true;
    }

    public boolean prev() {
        mPlayer.prev();
        return true;
    }

    public boolean next() {
        mPlayer.next();
        return true;
    }

    public boolean stop() {
        mPlayer.stop();
        return true;
    }

}
