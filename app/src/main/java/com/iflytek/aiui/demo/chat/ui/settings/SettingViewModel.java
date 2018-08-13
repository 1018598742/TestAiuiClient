package com.iflytek.aiui.demo.chat.ui.settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.repository.SettingsRepo;

import javax.inject.Inject;

/**
 * Created by PR on 2017/12/14.
 */

public class SettingViewModel extends ViewModel {
    private SettingsRepo mSettingsRepo;
    @Inject
    public SettingViewModel(SettingsRepo settingsRepo) {
        mSettingsRepo = settingsRepo;
    }

    //从preference中同步最新的setting设置
    public void syncLastSetting() {
        mSettingsRepo.updateSettings();
    }

    //唤醒是否可用
    public LiveData<Boolean> isWakeUpAvailable() {
        return mSettingsRepo.getWakeUpEnableState();
    }

    public LiveData<Boolean> isTTSAvailable() {
        return mSettingsRepo.getTTSEnableState();
    }
}

