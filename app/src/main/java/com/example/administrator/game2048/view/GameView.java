package com.example.administrator.game2048.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;

import com.example.administrator.game2048.activity.Game;
import com.example.administrator.game2048.bean.GameItem;
import com.example.administrator.game2048.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/31.
 */
public class GameView extends GridLayout implements View.OnTouchListener {

    //最高纪录
    private int mHighScore;
    //历史记录分数
    private int mScoreHistory;
    //目标分数
    private int mTarget;
    //矩阵行列数，游戏行列数。
    private int mGameLines;
    //GameView对应矩阵
    private GameItem[][] mGameMatrix;
    //历史记录数组
    private int[][] mGameMatrixHistory;
    //辅助数组
    private List<Integer> mCalList;
    //空格list 不显示数字的list
    private List<Point> mBanks;
    //记录坐标
    private int mStartX, mStartY, mEndX, mEndY;
    private int mKeyItemNum = -1;


    public GameView(Context context) {
        super(context);
        mTarget = Config.mSp.getInt(Config.KEY_GAME_GOAL, 2048);
        initGameMatrix();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameMatrix();
    }

    /**
     * 初始化View
     */
    private void initGameMatrix() {
        //初始化矩阵
        removeAllViews();
        mScoreHistory = 0;
        Config.SCROE = 0;
        Config.mGameLines = Config.mSp.getInt(Config.KEY_GAME_LINES, 4);
        mGameLines = Config.mGameLines;
        mGameMatrix = new GameItem[mGameLines][mGameLines];
        mGameMatrixHistory = new int[mGameLines][mGameLines];

        mCalList = new ArrayList<Integer>();
        mBanks = new ArrayList<Point>();
        mHighScore = Config.mSp.getInt(Config.KEY_HIGH_SCROE, 0);

        setColumnCount(mGameLines);
        setRowCount(mGameLines);
        setOnTouchListener(this);

        //初始化Veiw参数
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        Config.mItemSize = metrics.widthPixels / Config.mGameLines;

        initGameView(Config.mItemSize);
    }

    private void initGameView(int cardSize) {
        removeAllViews();
        GameItem card;

        for (int i = 0; i < mGameLines; i++) {

            for (int j = 0; j < mGameLines; j++) {
                card = new GameItem(getContext(), 0);
                addView(card, cardSize, cardSize);
                //初始化GameMatrix全部为0 空格list为所有
                mGameMatrix[i][j] = card;
                mBanks.add(new Point(i, j));
            }
        }

        //添加两个itemview
        addRandomNum();
        addRandomNum();
    }

    /**
     * 添加随机数字
     */
    private void addRandomNum() {
        getBanks();

        if (mBanks.size() > 0) {
            int randomNum = (int) (Math.random() * mBanks.size());
            Point randomPoint = mBanks.get(randomNum);

            mGameMatrix[randomPoint.x][randomPoint.y].setNum(Math.random() > 0.2d ? 2 : 4);
        }
    }

    /**
     * 获取空格Item数组
     */
    private void getBanks() {
        mBanks.clear();
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == 0) {
                    mBanks.add(new Point(i, j));
                }
            }
        }
    }


    /**
     * @param v
     * @param event
     * @return
     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveHistoryMatrix();
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mEndX = (int) event.getX();
                mEndY = (int) event.getY();
                judgeDirection(mEndX - mStartX, mEndY - mStartY);
                if (isMoved()) {
                    addRandomNum();
                    //修改显示分数
                    Game.getGameActivity().setScore(Config.SCROE, 0);
                }
                checkCompleted();
                break;
            default:
                break;

        }
        return true;
    }

    /**
     * 判断是否移动过（是否需要新增Item)
     *
     * @return 是否移动
     */
    private boolean isMoved() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrixHistory[i][j] != mGameMatrix[i][j].getNum()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 撤销上次移动
     */
    public void revertGame() {
        //第一次不可以撤销
        int sum = 0;
        for (int[] element : mGameMatrixHistory) {
            for (int i : element) {
                sum += i;
            }
        }
        if (sum != 0) {
            Game.getGameActivity().setScore(mScoreHistory, 0);
            Config.SCROE = mScoreHistory;
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    mGameMatrix[i][j].setNum(mGameMatrixHistory[i][j]);
                }
            }
        }
    }

    /**
     * 判断移动方向
     * @param offsetX
     * @param offsetY
     */
    public void judgeDirection(int offsetX, int offsetY) {
        int density = getDeviceDensity();
        int slideDis = 5 * density;
        int maxDis = 200 * density;
        boolean flagNormal =
                (Math.abs(offsetX) > slideDis || Math.abs(offsetY) > slideDis) &&
                        (Math.abs(offsetX) < maxDis) &&
                        (Math.abs(offsetY) < maxDis);
        boolean flagSuper = Math.abs(offsetX) > maxDis || Math.abs(offsetY) > maxDis;

        if (flagNormal && !flagSuper) {
            if (Math.abs(offsetX) > Math.abs(offsetY)) {
                if (offsetX > slideDis) {
                    swipeRight();
                } else {
                    swipeLeft();
                }
            } else {
                if (offsetY > slideDis) {
                    swipeDown();
                } else {
                    swipeUp();
                }
            }
        } else if (flagSuper) {//启动超级用户权限来添加自定义数字
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getContext());
            final EditText et = new EditText(getContext());
            builder.setTitle("Back Door").setView(et)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!TextUtils.isEmpty(et.getText())) {
                                        addSuperNum(Integer.parseInt(et.getText().toString()));
                                        checkCompleted();
                                    }
                                }
                            })
                    .setNegativeButton("ByeBye", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        }
    }

    private int getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return (int) metrics.density;
    }

    /**
     * 判断是否结束
     * <p/>
     * 0:结束 1:正常 2:成功
     */
    private void checkCompleted() {
        int result = checkNums();

        if (result == 0) {
            if (Config.SCROE > mHighScore) {
                SharedPreferences.Editor editor = Config.mSp.edit();
                editor.putInt(Config.KEY_HIGH_SCROE, Config.SCROE);
                editor.apply();
                Game.getGameActivity().setScore(Config.SCROE, 0);
                Config.SCROE = 0;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Gome over")
                    .setPositiveButton("Again",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startGame();
                                }
                            }).create().show();
            Config.SCROE = 0;

        } else if (result == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mission Accomplished")
                    .setPositiveButton("Again",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startGame();
                                }
                            })
                    .setNegativeButton("Continue",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //继续游戏，修改target
                                    SharedPreferences.Editor editor = Config.mSp.edit();
                                    if (mTarget == 1024) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 2048);
                                        mTarget = 2048;
                                        Game.getGameActivity().setGoal(2048);
                                    } else if (mTarget == 2048) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    } else {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    }
                                    editor.apply();
                                }
                            }).create().show();
            Config.SCROE = 0;
        }

    }

    public void startGame() {
        initGameMatrix();
        initGameView(Config.mItemSize);
    }

    /**
     * 检查所有的数字 看是否有满足条件的
     *
     * @return 0:结束 1:正常 2:成功
     */
    private int checkNums() {
        getBanks();
        if (mBanks.size() == 0) {
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    if (j < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i][j + 1].getNum()) {
                            return 1;
                        }
                    }
                    if (i < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i + 1][j].getNum()) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == mTarget) {
                    return 2;
                }
            }
        }
        return 1;
    }

    /**
     * @param num
     */
    private void addSuperNum(int num) {
        if (checkSuperNum(num)) {
            getBanks();
            if (mBanks.size() > 0) {
                int randomNum = (int) (Math.random() * mBanks.size());
                Point randomPoint = mBanks.get(randomNum);
                mGameMatrix[randomPoint.x][randomPoint.y].setNum(num);
                animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
            }
        }
    }

    /**
     * 判断添加的数字是否合法
     *
     * @param num
     * @return
     */
    private boolean checkSuperNum(int num) {
        boolean flag = (num == 2 || num == 4 || num == 8 || num == 16
                || num == 32 || num == 64 || num == 128 || num == 256
                || num == 512 || num == 1024 || num == 2048);
        return flag;

    }

    /**
     * 生成动画
     *
     * @param target
     */
    private void animCreate(GameItem target) {
        ScaleAnimation sa = new ScaleAnimation(0.1f, 1, 0.1f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(300);
        target.setAnimation(null);
        target.getItemView().setAnimation(sa);
    }

    /**
     * 保存历史记录
     */
    private void saveHistoryMatrix() {
        mScoreHistory = Config.SCROE;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                mGameMatrixHistory[i][j] = mGameMatrix[i][j].getNum();
            }
        }
    }


    /**
     * 滑动事件：左
     */
    private void swipeLeft() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }

            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }

            //改变Item的值
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(mCalList.get(j));
            }

            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(0);
            }

            //重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 滑动事件：右
     */
    private void swipeRight() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }

    /**
     * 滑动事件：上
     */
    private void swipeUp() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(mCalList.get(j));
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(0);
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 滑动事件：下
     */
    private void swipeDown() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCROE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }

}