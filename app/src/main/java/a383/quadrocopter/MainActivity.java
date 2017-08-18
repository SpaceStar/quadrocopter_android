package a383.quadrocopter;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String SERVER_IP = "192.168.1.1";
    public static final int SERVER_PORT = 8888;

    public static final long UDP_PERIOD = 50;
    public static final long WIFI_PERIOD = 500;
    public static final int STATE_SIZE = 5;

    private boolean mWifiDisable;

    protected TextView mStateText;
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
    private Timer mUdpTimer;
    private Timer mWifiTimer;
    private WifiManager mWifiManager;

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
        initWifi();

        state = new byte[STATE_SIZE];

        mUdpClient = new UdpClient(SERVER_IP, SERVER_PORT);

        mUdpTimer = new Timer();
        mUdpTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUdpClient.sendBytes(state);
            }
        }, 0, UDP_PERIOD);

        mWifiTimer = new Timer();
        mWifiTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                WifiInfo currentWifi = mWifiManager.getConnectionInfo();
                if ((currentWifi.getNetworkId() != -1) && (currentWifi.getSSID().equals("\"" + getApplicationContext().getResources().getString(R.string.wifi_ssid) + "\""))) {
                    runOnUiThread(() -> mStateText.setText(String.valueOf(currentWifi.getRssi())));
                } else {
                    runOnUiThread(() -> mStateText.setText(R.string.state_no_connection));
                }
            }
        }, 0, WIFI_PERIOD);
    }

    private void initViews() {
        mStateText = ((TextView) findViewById(R.id.controller_signal_state));
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

    private void initWifi() {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + getApplicationContext().getResources().getString(R.string.wifi_ssid) + "\"";
        conf.preSharedKey = "\"" + getApplicationContext().getResources().getString(R.string.wifi_pass) + "\"";

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiDisable = true;
            mWifiManager.setWifiEnabled(true);
        }
        int networkId = mWifiManager.addNetwork(conf);

        mWifiManager.disconnect();
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUdpClient != null) {
            mUdpClient.stopClient();
        }
        if (mUdpTimer != null) {
            mUdpTimer.cancel();
        }
        if (mWifiTimer != null) {
            mWifiTimer.cancel();
        }
        if (mWifiDisable) {
            mWifiManager.setWifiEnabled(false);
        }
    }
}
