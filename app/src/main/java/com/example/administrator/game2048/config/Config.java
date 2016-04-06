package com.example.administrator.game2048.config;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/3/31.
 */
public class Config extends Application {

    public static SharedPreferences mSp;

    public static int mGameGoal;

    public static int mGameLines;

    //Item宽高
    public static int mItemSize;

    /**
     * 记录分数
     */
    public static int SCROE = 0;

    public static String SP_HIGH_SCROE = "SP_HIGHSCROE";

    public static String KEY_HIGH_SCROE = "KEY_HighScore";

    public static String KEY_GAME_LINES = "KEY_GAMELINES";

    public static String KEY_GAME_GOAL = "kEY_GameGoal";

    @Override
    public void onCreate() {
        super.onCreate();
        mSp = getSharedPreferences(SP_HIGH_SCROE,0);
        mGameLines = mSp.getInt(KEY_GAME_LINES, 4);
        mGameGoal = mSp.getInt(KEY_GAME_GOAL, 2048);
        mItemSize = 0;
    }
}

