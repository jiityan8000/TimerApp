package com.jiityan.excuseme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.media.SoundPool;
import android.os.Build;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private TextView timerText;

    private Timer timer;
    private CountDownTimerTask timerTask = null;
    private Handler handler = new Handler();
    private long count = 0, tmp_count;

    private int soundOne; //, soundTwo, soundThree;

    private SeekBar minBar, secBar;

    private  int status=0;  //0なら停止中, 1なら実行中, -1なら一時停止中

    //サウンド系
    @SuppressWarnings("deprecation")
    private SoundPool buildSoundPool(int poolMax) {
        SoundPool pool = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            pool = new SoundPool(poolMax, AudioManager.STREAM_ALARM, 0);
        } else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build();

            pool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(poolMax)
                    .build();
        }

        return pool;
    }

    final int SOUND_POOL_MAX = 3;
    SoundPool pool = buildSoundPool(SOUND_POOL_MAX);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = (TextView)findViewById(R.id.timer);
        timerText.setText("00:00");

        soundOne = pool.load(this, R.raw.chime13, 1);
        //soundTwo = pool.load(this, R.raw.atsumori, 2);
        //soundThree = pool.load(this, R.raw.mach_henshin_low, 3);

        minBar = (SeekBar)findViewById(R.id.minbar);
        minBar.setProgress(0);
        secBar = (SeekBar)findViewById(R.id.secbar);
        secBar.setProgress(0);

        // タイマー開始
        startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // タイマーが走っている最中にボタンをタップされたケース
                if(null != timer){
                    if(null != timer){ //タイマー実行中に長押しされた場合
                        // Cancel
                        timer.cancel();
                        timer = null;
                        count = 0;
                        tmp_count = 0;
                        status = 0;
                        minBar.setProgress(0);
                        secBar.setProgress(0);
                        timerText.setText("00:00");
                        startButton.setText(String.format("Start"));
                    } else {  //タイマー停止中に長押しされた場合
                        count = 0;
                        minBar.setProgress(0);
                        secBar.setProgress(0);
                        timerText.setText("0:00");
                        startButton.setText(String.format("Start"));
                    }
                } else { //タイマー停止中にボタンをタップされたケース
                    if(status==-1) {
                        count = tmp_count;
                    }
                    if (count > 0) {
                        status  = 1;
                        // Timer インスタンスを生成
                        timer = new Timer();
                        // TimerTask インスタンスを生成
                        timerTask = new CountDownTimerTask();
                        // スケジュールを設定 1000msec
                        // public void schedule (TimerTask task, long delay, long period)
                        timer.schedule(timerTask, 0, 1000);
                        startButton.setText(String.format("Reset"));
                    }
                }
            }
        });

        startButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(null != timer){ //タイマー実行中に長押しされた場合
                    // Cancel
                    timer.cancel();
                    timer = null;
                    count = 0;
                    tmp_count = 0;
                    status = 0;
                    minBar.setProgress(0);
                    secBar.setProgress(0);
                    timerText.setText("00:00");
                } else {  //タイマー停止中に長押しされた場合
                    count = 0;
                    minBar.setProgress(0);
                    secBar.setProgress(0);
                    timerText.setText("0:00");
                }
                return true;
            }
        });

        minBar.setMax(60);
        minBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar minBar, int promin, boolean fromUser) {
                long ss = count % 60;
                timerText.setText(String.format("%1$02d:%2$02d", promin, ss));
                count = promin * 60 + ss;
                //text.setText(String.format("count = %1d", count));
            }
            public void onStartTrackingTouch(SeekBar minBar) {
            }
            public void onStopTrackingTouch(SeekBar minBar) {
            }
        });

        secBar.setMax(59);
        secBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar secBar, int prosec, boolean fromUser2) {
                long mm = count / 60;
                // 桁数を合わせるために02d(2桁)を設定
                timerText.setText(String.format("%1$02d:%2$02d", mm, prosec));
                count = mm * 60 + prosec;
                //text.setText(String.format("count = %1d", count));
            }
            public void onStartTrackingTouch(SeekBar secBar) {
            }
            public void onStopTrackingTouch(SeekBar secBar) {
            }
        });
    }

    class CountDownTimerTask extends TimerTask {
        @Override
        public void run() {
            // handlerを使って処理をキューイングする
            handler.post(new Runnable() {
                public void run() {
                    count--;
                    long mm = count / 60;
                    long ss = count % 60;
                    // 桁数を合わせるために$02d(2桁)を設定
                    timerText.setText(String.format("%1$02d:%2$02d", mm, ss));
                    //text.setText(String.format("count=%1$02d", count));
                    minBar.setProgress((int)mm);
                    secBar.setProgress((int)ss);
                    if (count <= 0) {
                        timer.cancel();
                        pool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                        timer = null;
                        tmp_count = 0;
                    }
                }
            });
        }
    }
}