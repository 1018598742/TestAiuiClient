package com.iflytek.aiui.demo.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.iflytek.aiui.demo.chat.ui.about.AboutFragment;
import com.iflytek.aiui.demo.chat.ui.chat.ChatFragment;
import com.iflytek.aiui.demo.chat.ui.detail.DetailFragment;
import com.iflytek.aiui.demo.chat.ui.settings.SettingsFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class ChatActivity extends AppCompatActivity implements HasSupportFragmentInjector{
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    private SettingsFragment mSettingsFragment;
    private ChatFragment mChatFragment;
    private AboutFragment mAboutFragment;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    // 矢量图兼容支持
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(ChatActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        onCreateFinish();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    protected void onCreateFinish() {
        mChatFragment = new ChatFragment();
        mSettingsFragment = new SettingsFragment();
        mAboutFragment = new AboutFragment();

        //切换到ChatFragment聊天交互界面
        switchChats();
    }

    /**
     * 切换到设置页面
     */
    public void switchToSettings() {
        switchFragment(mSettingsFragment, true);
    }

    /**
     * 切换到聊天交互页面
     */
    public void switchChats() {
        switchFragment(mChatFragment, false);
    }

    /**
     * 切换到关于页面
     */
    public void switchToAbout() {
        switchFragment(mAboutFragment, true);
    }

    /**
     *
     * 切换到语义详情页
     */
    public void switchToDetail(String content) {
        switchFragment(DetailFragment.createDetailFragment(content), true);
    }

    protected void switchFragment(Fragment fragment, boolean backStack) {
        //收回抽屉动画
        drawer.closeDrawers();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(backStack) {
            fragmentTransaction.addToBackStack(null);
        }
        //设置fragment切换的滑动动画
        if(fragment == mChatFragment) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_right_in, R.anim.slide_left_out,
                    R.anim.slide_left_in, R.anim.slide_right_out);
        } else {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_left_in, R.anim.slide_right_out,
                    R.anim.slide_right_in, R.anim.slide_left_out);
        }

        fragmentTransaction.replace(R.id.container, fragment) .commitAllowingStateLoss();
    }

    private void setupActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //设置ActionBar的title，icon
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(FragmentManager fm, Fragment f) {
                super.onFragmentResumed(fm, f);
                if(f instanceof ChatFragment) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setTitle("AIUI");
                }else if(f instanceof SettingsFragment){
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("设置");
                }else if(f instanceof DetailFragment) {
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("详情");
                }else if(f instanceof AboutFragment) {
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("关于");
                }
            }
        }, false);

        //ActionBar的返回按钮监听
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().popBackStack();
            }
        });

        //设置侧边栏按钮跳转
        NavigationView navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_settings: {
                        switchToSettings();
                        break;
                    }

                    case R.id.nav_about: {
                        switchToAbout();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
