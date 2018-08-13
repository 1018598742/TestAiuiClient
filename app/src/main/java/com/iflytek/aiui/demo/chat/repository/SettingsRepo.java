package com.iflytek.aiui.demo.chat.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.BuildConfig;
import com.iflytek.aiui.demo.chat.model.Settings;
import org.json.JSONObject;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * 设置repo
 */

@Singleton
public class SettingsRepo {
    public static final String KEY_AIUI_WAKEUP = "aiui_wakeup";
    public static final String KEY_DEFAULT_APPID = "last_appid";
    public static final String KEY_DEFAULT_KEY = "last_key";
    public static final String KEY_DEFAULT_SCENE = "last_scene";
    public static final String KEY_APPID = "appid";
    public static final String KEY_APP_KEY = "key";
    public static final String KEY_SCENE = "scene";
    public static final String AIUI_TTS = "aiui_tts";

    private Context mContext;
    private String mDefaultAppid;
    private String mDefaultAppKey;
    private String mDefaultScene;
    private MutableLiveData<Settings> mSettings = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveWakeUpEnable = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveTTSEnable = new MutableLiveData<>();

    @Inject
    public SettingsRepo(Context context, @Named("AIUI cfg") JSONObject config) {
        mContext = context;
        //保存配置文件中默认的appid和key,scene
        mDefaultAppid = config.optJSONObject("login").optString(KEY_APPID);
        mDefaultAppKey = config.optJSONObject("login").optString(KEY_APP_KEY);
        mDefaultScene = config.optJSONObject("global").optString(KEY_SCENE);

        mSettings.postValue(getLatestSettings(mContext));
    }

    public void config(String appid, String key, String scene) {
        //设置新的appid，key及scene，更新到sharePreference中
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_APPID, appid);
        editor.putString(KEY_APP_KEY, key);
        editor.putString(KEY_SCENE, scene);
        editor.commit();

        updateSettings();
    }

    public LiveData<Settings> getSettings() {
        return mSettings;
    }

    public void updateSettings() {
        //通知监听配置更新
        mSettings.postValue(getLatestSettings(mContext));
    }

    public LiveData<Boolean> getWakeUpEnableState() {
        return mLiveWakeUpEnable;
    }

    public LiveData<Boolean> getTTSEnableState() {
        return mLiveTTSEnable;
    }

    @NonNull
    private Settings getLatestSettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastConfigAppid = preferences.getString(KEY_DEFAULT_APPID, "");
        String lastConfigKey = preferences.getString(KEY_DEFAULT_KEY, "");
        String lastConfigScene = preferences.getString(KEY_DEFAULT_SCENE, "");
        //不同说明APK重装更新了assets下的aiui.cfg，将新的appid，key的设置同步到所有的配置
        if(!lastConfigAppid.equals(mDefaultAppid) || !lastConfigKey.equals(mDefaultAppKey) ||
                !lastConfigScene.equals(mDefaultScene)) {
            syncDefaultConfig(preferences);
            restoreDefaultConfig(preferences);
        }

        //将appid和key为空时恢复默认appid，key
        if(TextUtils.isEmpty(preferences.getString(KEY_APPID, "")) && TextUtils.isEmpty(
                preferences.getString(KEY_APP_KEY, ""))) {
            restoreDefaultConfig(preferences);
        }

        //因为唤醒资源和appid绑定，当前appid不为默认配置时禁止唤醒功能开启
        if(!mDefaultAppid.equals(preferences.getString(KEY_APPID, ""))) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_AIUI_WAKEUP, false);
            editor.commit();
            mLiveWakeUpEnable.postValue(false);
        } else {
            mLiveWakeUpEnable.postValue(BuildConfig.WAKEUP_ENABLE);
        }

        Settings settings = new Settings();
        settings.wakeup = preferences.getBoolean(KEY_AIUI_WAKEUP, false);
        settings.bos = Integer.valueOf(preferences.getString("aiui_bos", "5000"));
        settings.eos = Integer.valueOf(preferences.getString("aiui_eos", "1000"));
        settings.debugLog = preferences.getBoolean("aiui_debug_log", true);
        settings.saveDebugLog = preferences.getBoolean("aiui_save_datalog", false);
        settings.appid = preferences.getString(KEY_APPID, "");
        settings.key = preferences.getString(KEY_APP_KEY, "");
        settings.scene = preferences.getString(KEY_SCENE, "");
        settings.tts = preferences.getBoolean(AIUI_TTS, false);

        mLiveTTSEnable.postValue(settings.tts);

        return settings;
    }

    /**
     * 更新默认配置
     * @param preferences 当前配置的SharePreference
     */
    private void syncDefaultConfig(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEFAULT_APPID, mDefaultAppid);
        editor.putString(KEY_DEFAULT_KEY, mDefaultAppKey);
        editor.putString(KEY_DEFAULT_SCENE, mDefaultScene);
        editor.commit();
    }

    /**
     * 恢复appid,key到默认配置
     * @param preferences 当前配置的SharePreference
     */
    private void restoreDefaultConfig(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_APPID, mDefaultAppid);
        editor.putString(KEY_APP_KEY, mDefaultAppKey);
        editor.putString(KEY_SCENE, mDefaultScene);
        editor.commit();
    }
}
