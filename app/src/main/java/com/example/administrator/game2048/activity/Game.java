package com.example.administrator.game2048.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.game2048.R;
import com.example.administrator.game2048.config.Config;
import com.example.administrator.game2048.view.GameView;

/**
 * Created by Administrator on 2016/3/31.
 */
public class Game extends Activity implements View.OnClickListener {

    //Activity的引用
    private static Game mGame;
    private int mHighScore;
    private int mGoal;

    //记录分数
    private TextView mTvScore;
    //记录历史分数
    private TextView mTvHighScore;
    //记录目标分数
    private TextView mTvGoal;

    //重新开始， 撤销， 选项按钮
    private Button mBtnRestart;
    private Button mBtnRevert;
    private Button mBtnOption;

    //游戏面板
    private GameView mGameView;

    public Game() {
        mGame = this;
    }

    /**
     * 获取当前Activity的引用
     *
     * @return Activity.this
     */
    public static Game getGameActivity() {
        return mGame;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化view
        initView();

        mGameView = new GameView(this);
        //让游戏面板居中
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.game_panel);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.game_panel_rl);
        relativeLayout.addView(mGameView);
    }

    private void initView() {
        mTvScore = (TextView) findViewById(R.id.scroe);
        mTvHighScore = (TextView) findViewById(R.id.record);
        mTvGoal = (TextView) findViewById(R.id.tv_goal);

        mBtnRevert = (Button) findViewById(R.id.btn_revert);
        mBtnRestart = (Button) findViewById(R.id.btn_restart);
        mBtnOption = (Button) findViewById(R.id.btn_option);

        mBtnRevert.setOnClickListener(this);
        mBtnRestart.setOnClickListener(this);
        mBtnOption.setOnClickListener(this);

        mHighScore = Config.mSp.getInt(Config.KEY_HIGH_SCROE, 0);
        mGoal = Config.mSp.getInt(Config.KEY_GAME_GOAL, 2048);

        mTvHighScore.setText("" + mHighScore);
        mTvScore.setText("0");
        mTvGoal.setText("" + mGoal);

        setScore(0, 0);
    }

    public void setGoal(int num) {
        mTvGoal.setText(String.valueOf(num));
    }

    /**
     * 修改分数
     *
     * @param score
     * @param flag
     */
    public void setScore(int score, int flag) {
        switch (flag) {
            case 0:
                mTvScore.setText("" + score);
                break;
            case 1:
                mTvHighScore.setText("" + score);
                break;
            default:
                break;
        }
    }

    /**
     * 获取最高纪录
     */
    public void getHighScore() {
        int score = Config.mSp.getInt(Config.KEY_HIGH_SCROE, 0);
        setScore(score, 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_restart:
                mGameView.startGame();
                break;
            case R.id.btn_revert:
                mGameView.revertGame();
                break;
            case R.id.btn_option:
                Intent intent = new Intent(Game.this, ConfigPreference.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mGoal = Config.mSp.getInt(Config.KEY_GAME_GOAL, 0);
            mTvGoal.setText("" + mGoal);
            getHighScore();
            mGameView.startGame();
        }
    }
}
