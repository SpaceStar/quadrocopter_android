package a383.quadrocopter;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String SERVER_IP = "192.168.1.1";
    public static final int SERVER_PORT = 8888;

    public static final long PERIOD = 50;
    public static final int STATE_SIZE = 5;

    protected SeekBar mSeekBar;
    protected Button mArmButton;
    protected Button mStopButton;
    protected Button llButton;
    protected Button lrButton;
    protected Button rlButton;
    protected Button rrButton;
    protected Button ruButton;
    protected Button rdButton;

    private UdpClient mUdpClient;
    private Timer mSignalTimer;

    byte state[];

    private enum channel {
        GAS, YAW, PITCH, ROLL, OPTIONS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initViews();

        state = new byte[STATE_SIZE];

        mUdpClient = new UdpClient(SERVER_IP, SERVER_PORT);

        mSignalTimer = new Timer();
        mSignalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUdpClient.sendBytes(state);
            }
        }, 0, PERIOD);
    }

    private void initViews() {
        mSeekBar = ((SeekBar) findViewById(R.id.controller_seekbar));
        mArmButton = ((Button) findViewById(R.id.controller_button_u));
        mStopButton = ((Button) findViewById(R.id.controller_button_d));
        llButton = ((Button) findViewById(R.id.controller_button_ll));
        lrButton = ((Button) findViewById(R.id.controller_button_lr));
        rlButton = ((Button) findViewById(R.id.controller_button_rl));
        rrButton = ((Button) findViewById(R.id.controller_button_rr));
        ruButton = ((Button) findViewById(R.id.controller_button_ru));
        rdButton = ((Button) findViewById(R.id.controller_button_rd));

        mSeekBar.setMax(255);
        mSeekBar.setEnabled(false);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                state[channel.GAS.ordinal()] = (byte)progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mArmButton.setOnLongClickListener(v -> {
            boolean check = true;
            for (int i = 0; i < STATE_SIZE - 1; i++)
                if (state[i] != 0)
                    check = false;

            if (check) {
                state[channel.OPTIONS.ordinal()] ^= 1;

                if ((state[channel.OPTIONS.ordinal()] & 1) == 1) {
                    mArmButton.setText(R.string.arm_off);
                    mArmButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                    mSeekBar.setEnabled(true);
                } else {
                    mArmButton.setText(R.string.arm_on);
                    mArmButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                    mSeekBar.setEnabled(false);
                }
            }

            return  true;
        });

        mStopButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mSeekBar.setProgress(0);

                state[channel.OPTIONS.ordinal()] &= ~1;
                mArmButton.setText(R.string.arm_on);
                mArmButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                mSeekBar.setEnabled(false);
            }

            return true;
        });

        View.OnTouchListener onTouchListener = (v, event) -> {
            Button view = (Button) v;
            channel ch;
            byte value;

            switch (view.getId()) {
                case R.id.controller_button_ll:
                    ch = channel.YAW;
                    value = -128;
                    break;
                case R.id.controller_button_lr:
                    ch = channel.YAW;
                    value = 127;
                    break;
                case R.id.controller_button_rl:
                    ch = channel.ROLL;
                    value = -128;
                    break;
                case R.id.controller_button_rr:
                    ch = channel.ROLL;
                    value = 127;
                    break;
                case R.id.controller_button_ru:
                    ch = channel.PITCH;
                    value = 127;
                    break;
                case R.id.controller_button_rd:
                    ch = channel.PITCH;
                    value = -128;
                    break;
                default:
                    ch = null;
                    value = 0;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    state[ch.ordinal()] += value;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    state[ch.ordinal()] -= value;
            }

            return true;
        };

        llButton.setOnTouchListener(onTouchListener);
        lrButton.setOnTouchListener(onTouchListener);
        rlButton.setOnTouchListener(onTouchListener);
        rrButton.setOnTouchListener(onTouchListener);
        ruButton.setOnTouchListener(onTouchListener);
        rdButton.setOnTouchListener(onTouchListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUdpClient != null) {
            mUdpClient.stopClient();
        }
        if (mSignalTimer != null) {
            mSignalTimer.cancel();
        }
    }
}
