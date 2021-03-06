package com.iflytek.aiui.demo.chat.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.demo.chat.ChatActivity;
import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.databinding.ChatFragmentBinding;
import com.iflytek.aiui.demo.chat.model.ChatMessage;
import com.iflytek.aiui.demo.chat.model.RawMessage;
import com.iflytek.aiui.demo.chat.model.Settings;
import com.iflytek.aiui.demo.chat.model.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.repository.player.PlayState;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.common.widget.PopupWindowFactory;
import com.iflytek.aiui.demo.chat.utils.LogUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * 聊天主界面Fragment
 */

// 设置支持矢量图
@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "srcCompat",
                method = "setImageDrawable")})
public class ChatFragment extends Fragment implements PermissionChecker {
    public static final Pattern emptyPattern = Pattern.compile("^\\s+$", Pattern.DOTALL);

    private static final String TAG = "ChatFragment";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    protected ChatViewModel mMessageModel;
    private PlayerViewModel mPlayerViewModel;
    //当前所有交互消息列表
    protected List<ChatMessage> mInteractMessages;

    private MessageListAdapter mMsgAdapter;
    protected ChatFragmentBinding mChatBinding;
    //按住录音动画控制类
    private PopupWindowFactory mVoicePop;
    private ImageView VolumeView;
    //当前状态，取值参考Constant中STATE定义
    private int mState;
    //唤醒波浪动画
    private boolean mWaveAnim = false;
    //是否检测到前端点，提示 ’为说话‘ 时判断使用
    private boolean mVadBegin = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mChatBinding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment,
                container, false);

        return mChatBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        mMessageModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(ChatViewModel.class);
                        mPlayerViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(PlayerViewModel.class);

                        if (!report.areAllPermissionsGranted()) {
                            mMessageModel.fakeAIUIResult(0, "permission", "请重启应用允许请求的权限");
                        }
                        //所有权限通过，初始化界面
                        onPermissionChecked();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @CallSuper
    protected void onPermissionChecked() {
        initChatView();
        initSendAction();
        initVoiceAction();
        setInputState(Constant.STATE_VOICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatBinding.visualizer.onResume();//波浪曲线的id
    }

    private void initVoiceAction() {
        //根据左下角图标切换输入状态
        mChatBinding.emotionVoice.setOnClickListener(view -> {
            setInputState(mState == Constant.STATE_VOICE ?
                    Constant.STATE_TEXT : Constant.STATE_VOICE);
            dismissKeyboard(view.getWindowToken());
        });

        //初始化居中显示的按住说话动画
        View view = View.inflate(getActivity(), R.layout.layout_microphone, null);
        VolumeView = view.findViewById(R.id.iv_recording_icon);
        mVoicePop = new PopupWindowFactory(getActivity(), view);

        //按住说话按钮
        mChatBinding.voiceText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMessageModel.stopTTS();
                    mVadBegin = false;
                    mPlayerViewModel.pause();
                    mChatBinding.voiceText.setPressed(true);
                    if (mChatBinding.voiceText.isPressed()) {
                        mVoicePop.showAtLocation(v, Gravity.CENTER, 0, 0);
                        setInputState(Constant.STATE_VOICE_INPUTTING);
                        mMessageModel.startRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mVadBegin) {
                        Toast.makeText(getContext(), "您好像并没有开始说话", Toast.LENGTH_LONG).show();
                    }
                    mChatBinding.voiceText.setPressed(false);
                    mVoicePop.dismiss();
                    setInputState(Constant.STATE_VOICE);
                    mMessageModel.stopRecord();
                    break;
            }
            return true;
        });

        mVoicePop.getPopupWindow().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mChatBinding.voiceText.setPressed(false);
                mMessageModel.stopRecord();
            }
        });
    }


    private void setInputState(int state) {
        mState = state;
        mChatBinding.setState(state);
        mChatBinding.executePendingBindings();
    }

    private void initSendAction() {
        //文本语义按钮监听
        mChatBinding.emotionSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSend();
            }
        });
        mChatBinding.emotionSend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    doSend();
                    return true;
                }
                return false;
            }
        });
    }

    private void doSend() {
        //文本语义
        String msg = mChatBinding.editText.getText().toString();
        if (!TextUtils.isEmpty(msg) && !emptyPattern.matcher(msg).matches()) {
            mMessageModel.sendText(msg);
            mChatBinding.editText.setText("");
        } else {
            Toast.makeText(getContext(), "发送内容不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    private void initChatView() {
        //初始化交互消息展示列表
        ScrollSpeedLinearLayoutManger layout = new ScrollSpeedLinearLayoutManger(getActivity());
        layout.setSpeedSlow();
        layout.setStackFromEnd(true);

        mChatBinding.chatList.setLayoutManager(layout);
        mMsgAdapter = new MessageListAdapter(this);
        mChatBinding.chatList.setAdapter(mMsgAdapter);
        mChatBinding.chatList.setClipChildren(true);
        mChatBinding.chatList.setVerticalScrollBarEnabled(true);
        mChatBinding.chatList.getItemAnimator().setChangeDuration(0);

        mMsgAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mChatBinding.chatList.smoothScrollToPosition(positionStart);
            }
        });

        //获取交互消息，更新展示
        Transformations.map(mMessageModel.getInteractMessages(), input -> {
            List<ChatMessage> interactMessages = new ArrayList<>();
            for (RawMessage message : input) {
                interactMessages.add(new ChatMessage(message, ChatFragment.this, mMessageModel, mPlayerViewModel));
            }
            return interactMessages;
        }).observe(this, messages -> {
            mInteractMessages = messages;
            mMsgAdapter.replace(messages);
            mChatBinding.executePendingBindings();

            //无任何消息的交互提示
            if (messages.size() == 0) {
                StringBuilder hello = new StringBuilder();
                hello.append("你好，很高兴见到你 :D");
                hello.append(IntentHandler.NEWLINE_NO_HTML);
                hello.append(IntentHandler.NEWLINE_NO_HTML);
                hello.append("你可以文本或者语音跟我对话，更多的功能在左上角的设置里进行探索吧");
                mMessageModel.fakeAIUIResult(0, "hello", hello.toString());
            }
        });

        //监听设置中唤醒开关的变化
        mMessageModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                if (settings.wakeup) {
                    //设置中启用唤醒，进入待唤醒模式
                    onWaitingWakeUp();
                    //监听唤醒状态信息
                    mMessageModel.getStateEvent().observe(ChatFragment.this, new Observer<AIUIEvent>() {
                        @Override
                        public void onChanged(@Nullable AIUIEvent aiuiEvent) {
                            switch (aiuiEvent.eventType) {
                                case AIUIConstant.EVENT_WAKEUP:
                                    onWakeUp();
                                    break;

                                case AIUIConstant.EVENT_SLEEP:
                                    onWaitingWakeUp();
                                    break;
                            }
                        }
                    });
                } else {
                    //唤醒关闭，进入按住说话的交互模式
                    setInputState(Constant.STATE_VOICE);
                    mMessageModel.getStateEvent().removeObservers(ChatFragment.this);
                }
            }
        });

        //监听VAD事件获取音量及前端后端点事件
        mMessageModel.getVADEvent().observe(this, new Observer<AIUIEvent>() {
            @Override
            public void onChanged(@Nullable AIUIEvent aiuiEvent) {
                if (aiuiEvent.eventType == AIUIConstant.EVENT_VAD) {
                    switch (aiuiEvent.arg1) {
                        case AIUIConstant.VAD_BOS:
                            mVadBegin = true;
                            break;

                        //前端点超时消息
                        case 3:
                        case AIUIConstant.VAD_EOS: {
                            //唤醒状态下检测到后端点自动进入待唤醒模式
                            if (mState == Constant.STATE_WAKEUP) {
                                onWaitingWakeUp();
                            }
                            break;
                        }

                        //音量消息
                        case AIUIConstant.VAD_VOL: {
                            int level = 5000 + 8000 * aiuiEvent.arg2 / 100;

                            //更新居中的音量信息
                            if (VolumeView != null && VolumeView.getDrawable().setLevel(level)) {
                                VolumeView.getDrawable().invalidateSelf();
                            }

                            //唤醒状态下更新底部的音量波浪动画
                            if (mState == Constant.STATE_WAKEUP) {
                                mChatBinding.visualizer.setVolume(level);
                            }
                        }
                    }
                }
            }
        });

        //播放器控制
        mChatBinding.playControlBar.controlSongName.setSelected(true);
        mChatBinding.setPlayer(mPlayerViewModel);
        //监听播放器状态，更新控制界面
        mPlayerViewModel.getPlayState().observe(this, new Observer<PlayState>() {
            @Override
            public void onChanged(@Nullable PlayState playState) {
                Log.i(LogUtil.TAG, "ChatFragment-onChanged: 播放状态发生了改变");
                mChatBinding.setPlayState(playState);
                final LinearLayout playControl = mChatBinding.playControlBar.playControl;
                final CoordinatorLayout controlContainer = mChatBinding.playControlBar.controlContainer;

                if (playState.active && playControl.getVisibility() == View.GONE) {
                    playControl.setVisibility(View.VISIBLE);
                    controlContainer.setVisibility(View.VISIBLE);

                    //滑动停止当前播放并隐藏播放控制条
                    SwipeDismissBehavior<View> swipe = new SwipeDismissBehavior();
                    swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                    swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
                        @Override
                        public void onDismiss(View view) {
                            controlContainer.setVisibility(View.GONE);
                            playControl.setVisibility(View.GONE);
                            mPlayerViewModel.stop();
                        }

                        @Override
                        public void onDragStateChanged(int state) {
                        }
                    });

                    //将隐藏的播放控制条恢复
                    CoordinatorLayout.LayoutParams coordinatorParams =
                            (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                    coordinatorParams.setBehavior(swipe);

                    AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
                    appearAnimation.setDuration(500);
                    playControl.startAnimation(appearAnimation);

                    CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                    tParams.setMargins(0, 0, 0, 0);
                    playControl.requestLayout();
                    playControl.setAlpha(1.0f);
                }
            }
        });
    }

    private void onWakeUp() {
        //唤醒自动停止播放
        mPlayerViewModel.pause();
        setInputState(Constant.STATE_WAKEUP);
        if (!mWaveAnim) {
            //底部音量动画
            mChatBinding.visualizer.startAnim();
            mWaveAnim = true;
        }
    }

    private void onWaitingWakeUp() {
        //进入待唤醒状态
        setInputState(Constant.STATE_WAITING_WAKEUP);
        mChatBinding.visualizer.stopAnim();
        mWaveAnim = false;
    }

    /**
     * 隐藏键盘
     *
     * @param windowToken
     */
    private void dismissKeyboard(IBinder windowToken) {
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    /**
     * 切换至语义结果详情页
     *
     * @param content
     */
    public void switchToDetail(String content) {
        ((ChatActivity) getActivity()).switchToDetail(content);
    }

    @Override
    public void onPause() {
        super.onPause();
        mChatBinding.visualizer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatBinding.visualizer.release();
    }

    @Override
    public void checkPermission(String permission, final Runnable success, final Runnable failed) {
        Dexter.withActivity(getActivity()).withPermission(permission)
                .withListener(new CompositePermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        super.onPermissionGranted(response);
                        if (success != null) {
                            success.run();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        super.onPermissionDenied(response);
                        if (failed != null) {
                            failed.run();
                        }
                    }
                }).check();
    }
}
