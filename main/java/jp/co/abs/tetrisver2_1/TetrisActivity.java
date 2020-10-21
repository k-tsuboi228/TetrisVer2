package jp.co.abs.tetrisver2_1;

import java.util.Random;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

public class TetrisActivity extends AppCompatActivity {

    private class FieldView extends SurfaceView {

        Random mRand = new Random(System.currentTimeMillis());

        int[][][] blocks = {
                {
                        {1,1},
                        {0,1},
                        {0,1}
                },
                {
                        {2,2},
                        {2,0},
                        {2,0}
                },
                {
                        {3,3},
                        {3,3}
                },
                {
                        {4,0},
                        {4,4},
                        {4,0}
                },
                {
                        {5,0},
                        {5,5},
                        {0,5}
                },
                {
                        {0,6},
                        {6,6},
                        {6,0}
                },
                {
                        {7},
                        {7},
                        {7},
                        {7}
                }
        };

        int[][] block = blocks[mRand.nextInt(blocks.length)];
        int block1[][]  = blocks[mRand.nextInt(blocks.length)];
        int posx = 4, posy;
        int mapWidth  = 10;
        int mapHeight = 23;
        int nextWidth = 17;
        int nextHeight = 20;
        int[][] map = new int[mapHeight][];
        int[][] next = new int[nextHeight][];
        int count = 0;
        int score;
        int line;
        int speed = 1;


        public FieldView(Context context) {
            super(context);

            setBackgroundColor(0xFFFFFFFF);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }
        // 盤面を作る処理
        public void initGame() {
            for (int y = 0; y < mapHeight; y++) {
                map[y] = new int[mapWidth];
                for (int x = 0; x < mapWidth; x++) {
                    map[y][x] = 0;
                }
            }

            for ( int b = 14; b < nextHeight; b++){
                next[b] = new int[nextWidth];
                for(int a = 14; a < nextWidth; a++){
                    next[b][a] = 0;
                }
            }
        }

        void GameOver() {
            for (int i = 0; i < 10; i++) {
                if (map[0][i] != 0) {
                    Log.d("i=", "" + i);
                    count = 1;

                    Intent intent = new Intent(TetrisActivity.this, EndActivity.class);
                    intent.putExtra("score",score);
                    intent.putExtra("line", line);
                    startActivity(intent);
                    finish();
                }
            }
        }

        // ブロックに形をつける
        private void paintMatrix(Canvas canvas, int[][] matrix, int offsetx, int offsety, int color) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            rect.getPaint().setColor(color);
            int h = matrix.length;
            int w = matrix[0].length;

            for (int y = 0; y < h; y ++) {
                for (int x = 0; x < w; x ++) {
                    if (matrix[y][x] != 0) {
                        int px = (x + offsetx) * 70;
                        int py = (y + offsety) * 70;
                        rect.setBounds(px, py, px + 69, py + 69);
                        rect.draw(canvas);
                    }
                }
            }
        }

        // ブロックが存在していいかどうか判別
        boolean check(int[][] block, int offsetx, int offsety) {
            if (offsetx < 0 || offsety < 0 ||
                    mapHeight < offsety + block.length ||
                    mapWidth < offsetx + block[0].length) {
                return false;
            }

            if(mapHeight < offsety + block.length *70){
                GameOver();
            }

            for (int y = 0; y < block.length; y ++) {
                for (int x = 0; x < block[y].length; x ++) {
                    if (block[y][x] != 0 && map[y + offsety][x + offsetx] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        // ブロックをマップ上に表示
        void mergeMatrix(int[][] block, int offsetx, int offsety) {
            for (int y = 0; y < block.length; y ++) {
                for (int x = 0; x < block[0].length; x ++) {
                    if (block[y][x] != 0) {
                        map[offsety + y][offsetx + x] = block[y][x];
                    }
                }
            }
        }

        //次に落下するブロックを指定位置に表示
        void mergeMatrix1(int[][] block, int offsetx, int offsety){
            for (int y = 0; y < next.length; y++) {
                for (int x = 0; x < next[0].length; x++) {
                    if (block[y][x] != 0) {
                        next = block;
                    }
                }
            }

        }


        // 列がそろったら消す
        void clearRows() {

            int deleteline = 0;
            // 埋まった行は消す。nullで一旦マーキング
            for (int y = 0; y < mapHeight; y ++) {
                boolean full = true;
                for (int x = 0; x < mapWidth; x ++) {
                    if (map[y][x] == 0) {
                        full = false;
                        break;
                    }
                }

                if (full) {
                    map[y] = null;
                }

            }

            // 新しいmapにnull以外の行を詰めてコピーする
            int[][] newMap = new int[mapHeight][];
            int y2 = mapHeight - 1;
            for (int y = mapHeight - 1; y >= 0; y--) {
                if (map[y] == null) {
                    deleteline++;
                    line++;
                    continue;
                } else {
                    newMap[y2--] = map[y];
                    switch (deleteline) {
                        case 1:
                            score += 10;
                            break;
                        case 2:
                            score += 30;
                            break;
                        case 3:
                            score += 50;
                            break;
                        case 4:
                            score += 80;
                            break;
                    }

                }
                invalidate();
                deleteline = 0;
            }

            // 消えた行数分新しい行を追加する
            for (int i = 0; i <= y2; i++) {
                int[] newRow = new int[mapWidth];
                for (int j = 0; j < mapWidth; j ++) {
                    newRow[j] = 0;
                }
                newMap[i] = newRow;
            }
            map = newMap;
        }

        // 盤面に色を付ける
        @Override
        protected void onDraw(Canvas canvas) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
           // rect.setBounds(0, 0, 1080, 1731);
           /* rect.setBounds(0,0,1080,1731);
            rect.getPaint().setColor(0xFFFFFFFF);
            rect.draw(canvas);
            canvas.translate(0, 0);
            */

           ShapeDrawable nextBlockView = new ShapeDrawable(new RectShape());
           nextBlockView.setBounds(750, 50, 1030, 850);
           nextBlockView.getPaint().setColor(0xFF696969);
           nextBlockView.draw(canvas);


           Paint paint = new Paint();
            rect.setBounds(0, 0, 700, 1731);
            rect.getPaint().setColor(0xFF696969);
            rect.draw(canvas);

            String row = String.valueOf(line);
            String level = String.valueOf(speed);
            String getScore = String.valueOf(score);
            paint.setTextSize(50);

            canvas.drawText("Line:" + row, 800, 1000, paint);
            canvas.drawText("Speed:" + level, 800, 1100, paint);
            canvas.drawText("Score:" + getScore, 800, 1200, paint);

            for (int y = 0; y < block.length; y++) {
                for (int x = 0; x < block[0].length; x++) {
                    switch (block[y][x]) {
                        case 1:
                            paintMatrix(canvas, block, posx, posy, 0xFF008000);
                            break;
                        case 2:
                            paintMatrix(canvas, block, posx, posy, 0xFF008080);
                            break;
                        case 3:
                            paintMatrix(canvas, block, posx, posy, 0xFFFFFF00);
                            break;
                        case 4:
                            paintMatrix(canvas, block, posx, posy, 0xFF800080);
                            break;
                        case 5:
                            paintMatrix(canvas, block, posx, posy, 0xFF00FF00);
                            break;
                        case 6:
                            paintMatrix(canvas, block, posx, posy, 0xFFFF0000);
                            break;
                        case 7:
                            paintMatrix(canvas, block, posx, posy, 0xFF0000FF);
                            break;
                    }
                }
            }

            if(count == 0){
                paintMatrix(canvas, map, 0, 0, 0xFF222222);
            }
            if(count == 1) {
                paintMatrix(canvas, map, 0, 0, 0xFF808080);
            }
        }
        // ブロックの回転処理
        int[][] rotate(final int[][] block) {
            int[][] rotated = new int[block[0].length][];
            for (int x = 0; x < block[0].length; x ++) {
                rotated[x] = new int[block.length];
                for (int y = 0; y < block.length; y ++) {
                    rotated[x][block.length - y - 1] = block[y][x];
                }
            }
            return rotated;
        }

        // ブロックの移動処理
        private float adjustX = 100.0f;
        private float adjustY = 100.0f;
        private float touchX;
        private float touchY;
        private float nowTouchX;
        private float nowTouchY;

        @Override
        public boolean performClick(){
            super.performClick();
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            super.onTouchEvent(event);
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    performClick();
                    touchX = event.getX();
                    touchY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    nowTouchX = event.getX();
                    nowTouchY = event.getY();
                    FlickCheck();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            mHandler.sendEmptyMessage(INVALIDATE);
            return true;
        }

        /**
         * どの方向にフリックしたかチェック
         */
        private void FlickCheck(){
            Log.d("FlickPoint", "startX:" + touchX + " endX:" + nowTouchX
                    + " startY:" + touchY + " endY:" + nowTouchY);
            // 左フリック
            if(touchX > nowTouchX) {
                if(touchX - nowTouchX > adjustX) {
                    if (check(block, posx - 1, posy)) {
                        posx = posx - 1;
                    }
                    return;
                }
            }
            // 右フリック
            if(nowTouchX > touchX) {
                if(nowTouchX - touchX > adjustX) {
                    if (check(block, posx + 1, posy)) {
                        posx = posx + 1;
                    }
                    return;
                }
            }
            // 上フリック
            if(touchY > nowTouchY) {
                if(touchY - nowTouchY > adjustY) {
                    int[][] newBlock = rotate(block);
                    if (check(newBlock, posx, posy)) {
                        block = newBlock;
                    }
                    return;
                }
            }
            // 下フリック
            if(nowTouchY > touchY) {
                if(nowTouchY - touchY > adjustY) {
                    int y = posy;
                    while (check(block, posx, y)) { y++; }
                    if (y > 0) posy = y - 1;
                    return;
                }
            }
        }

        public void startAnime() {
            mHandler.sendEmptyMessage(INVALIDATE);
            mHandler.sendEmptyMessage(DROPBLOCK);
        }

        public void stopAnime() {
            mHandler.removeMessages(INVALIDATE);
            mHandler.removeMessages(DROPBLOCK);
        }

        private static final int INVALIDATE = 1;
        private static final int DROPBLOCK = 2;
        // 時間経過でブロックを落とす
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (count == 0) {
                    switch (msg.what) {
                        case INVALIDATE:
                            invalidate();
                            break;
                        case DROPBLOCK:
                            if (check(block, posx, posy + 1)) {
                                posy++;
                            } else {
                                mergeMatrix(block, posx, posy);
                                clearRows();
                                posx = 4;
                                posy = 0;
                              //  block = blocks[mRand.nextInt(blocks.length)];
                                block = block1;
                                block1 = blocks[mRand.nextInt(blocks.length)];
                                mergeMatrix1(block1, 0, 0);
                            }

                            invalidate();
                            Message massage = new Message();
                            massage.what = DROPBLOCK;
                            if(line <=14) {
                                sendMessageDelayed(massage, 500);
                            }else if(line>=15 && line<=29){
                                sendMessageDelayed(massage, 300);
                                speed = 2;
                            }
                            else if(line >= 30 ) {
                                sendMessageDelayed(massage, 100);
                                speed = 3;
                            }
                            break;
                    }
                }
            }
        };

    }

    private BGMPlayer bgm;
    FieldView mFieldView;

    Boolean isChecked;

    private void setFieldView() {
        if (mFieldView == null) {
            mFieldView = new FieldView(getApplication());
            setContentView(mFieldView);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.bgm = new BGMPlayer(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setFieldView();
        SharedPreferences Count = PreferenceManager.getDefaultSharedPreferences(this);
        isChecked = Count.getBoolean("Switch", true);


        if(isChecked == true) {
            bgm.start();
        }else {
            bgm = null;
        }
        mFieldView.initGame();
        mFieldView.startAnime();
        Looper.myQueue().addIdleHandler(new Idler());

        String Title = "Score: ";
        String getScore = String.valueOf(mFieldView.score);

        getSupportActionBar().setTitle(Title + getScore);
        getDelegate().setTitle(Title + getScore);

        mFieldView.count = 0;

    }
    @Override
    protected void onPause() {
        super.onPause();
        bgm.stop();
        mFieldView.stopAnime();
        mFieldView.count = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFieldView.stopAnime();
    }

    class Idler implements MessageQueue.IdleHandler {
        public Idler() {
            super();
        }

        public final boolean queueIdle() {
            return false;
        }
    }

}