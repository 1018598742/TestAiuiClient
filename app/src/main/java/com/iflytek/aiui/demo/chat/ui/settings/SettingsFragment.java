package com.iflytek.aiui.demo.chat.ui.settings;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.iflytek.aiui.demo.chat.R;
import dagger.android.support.AndroidSupportInjection;

/**
 * 设置界面Fragment
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String AIUI_EOS = "aiui_eos";
    public static final String AIUI_WAKEUP = "aiui_wakeup";
    public static final String AIUI_TTS = "aiui_tts";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private SettingViewModel mSettingModel;
    private EditTextPreference eosPreference;
    private SwitchPreferenceCompat wakeupPreference;
    private SwitchPreferenceCompat ttsPreference;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        eosPreference = (EditTextPreference) (getPreferenceManager().findPreference(AIUI_EOS));
        eosPreference.setSummary(String.format("%sms", getPreferenceManager().getSharedPreferences().getString(AIUI_EOS, "1000")));
        wakeupPreference = (SwitchPreferenceCompat) getPreferenceManager().findPreference(AIUI_WAKEUP);
        ttsPreference = (SwitchPreferenceCompat) getPreferenceManager().findPreference(AIUI_TTS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSettingModel = ViewModelProviders.of(this, mViewModelFactory).get(SettingViewModel.class);
        //根据唤醒是否可用决定设置界面的唤醒enable或者disable
        mSettingModel.isWakeUpAvailable().observe(this, enable -> wakeupPreference.setEnabled(enable));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //监听后端点的设置变化
        if (AIUI_EOS.equals(s)) {
            String eos = sharedPreferences.getString(s, "1000");
            //判断设置值的合法性
            if (!isNumeric(eos)) {
                eosPreference.setText("1000");
                Snackbar.make(getView(), R.string.eos_invalid_tip, Snackbar.LENGTH_LONG).show();
            } else {
                //根据新设置的后端点值更新设置项的summary展示
                eosPreference.setSummary(String.format("%sms", eos));
            }
        }
    }


    private boolean isNumeric(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        //设置界面退出时同步配置通知其他监听者
        mSettingModel.syncLastSetting();
        super.onStop();
    }


}
