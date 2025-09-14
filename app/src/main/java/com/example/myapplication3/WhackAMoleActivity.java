package com.example.myapplication3;

import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class WhackAMoleActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView tvScore;
    private Button btnRestart;

    private Button[][] buttons = new Button[3][3];
    private int score = 0;
    private int moleRow = -1, moleCol = -1;
    private boolean moleVisible = false; // 当前地鼠是否存在
    private boolean gameOver = false;    // 游戏是否结束

    private Handler handler = new Handler();
    private Random random = new Random();
    private Runnable moleRunnable;
    private Runnable speedRunnable;

    private long moleInterval = 1000; // 初始刷新间隔
    private long speedIncreaseInterval = 10000; // 每10秒加快

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whack_a_mole);

        gridLayout = findViewById(R.id.grid_layout);
        tvScore = findViewById(R.id.tv_score);
        btnRestart = findViewById(R.id.btn_restart);

        btnRestart.setOnClickListener(v -> startGame());

        initBoard();
        startGame();
    }

    private void initBoard() {
        gridLayout.removeAllViews();
        int numRows = 3, numCols = 3;
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Button btn = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(marginPx, marginPx, marginPx, marginPx);
                btn.setLayoutParams(params);
                btn.setBackgroundColor(0xFFAAAAAA);
                btn.setText("");

                final int r = i, c = j;
                btn.setOnClickListener(v -> clickCell(r, c));

                gridLayout.addView(btn);
                buttons[i][j] = btn;
            }
        }
    }

    private void startGame() {
        score = 0;
        tvScore.setText("分数: 0");
        moleInterval = 1000;
        moleVisible = false;
        moleRow = -1;
        moleCol = -1;
        gameOver = false;

        if (moleRunnable != null) handler.removeCallbacks(moleRunnable);
        if (speedRunnable != null) handler.removeCallbacks(speedRunnable);

        moleRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameOver) showMole();
                if (!gameOver) handler.postDelayed(this, moleInterval);
            }
        };
        handler.post(moleRunnable);

        speedRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameOver && moleInterval > 200) moleInterval -= 100;
                if (!gameOver) handler.postDelayed(this, speedIncreaseInterval);
            }
        };
        handler.postDelayed(speedRunnable, speedIncreaseInterval);
    }

    private void showMole() {
        if (moleVisible) {
            endGame("漏点");
            return;
        }

        if (moleRow >= 0 && moleCol >= 0) {
            buttons[moleRow][moleCol].setText("");
            buttons[moleRow][moleCol].setBackgroundColor(0xFFAAAAAA);
        }

        moleRow = random.nextInt(3);
        moleCol = random.nextInt(3);
        buttons[moleRow][moleCol].setText("🐹");
        buttons[moleRow][moleCol].setBackgroundColor(0xFFFFCC00);
        moleVisible = true;
    }

    private void clickCell(int row, int col) {
        if (gameOver) return;

        if (row == moleRow && col == moleCol) {
            score++;
            tvScore.setText("分数: " + score);
            buttons[moleRow][moleCol].setText("");
            buttons[moleRow][moleCol].setBackgroundColor(0xFFAAAAAA);
            moleRow = -1;
            moleCol = -1;
            moleVisible = false;
        } else {
            endGame("点错");
        }
    }

    private void endGame(String reason) {
        if (gameOver) return; // 防止重复调用
        gameOver = true;

        if (moleRunnable != null) handler.removeCallbacks(moleRunnable);
        if (speedRunnable != null) handler.removeCallbacks(speedRunnable);

        if (moleRow >= 0 && moleCol >= 0) {
            buttons[moleRow][moleCol].setText("");
            buttons[moleRow][moleCol].setBackgroundColor(0xFFAAAAAA);
        }

        new AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage("原因: " + reason + "\n你的最终成绩: " + score)
                .setPositiveButton("重新开始", (dialog, which) -> startGame())
                .setNegativeButton("退出", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moleRunnable != null) handler.removeCallbacks(moleRunnable);
        if (speedRunnable != null) handler.removeCallbacks(speedRunnable);
    }
}
