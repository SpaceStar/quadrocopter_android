package com.spacestar.quadrocopter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import static com.spacestar.quadrocopter.Consts.*;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private boolean mWifiDisable;

    protected TextView mWifiStateText;
    protected TextView mBatteryStateText;
    protected SeekBar mSeekBar;
    protected Button mArmButton;
    protected Button mStopButton;
    protected Button llButton;
    protected Button lrButton;
    protected Button rlButton;
    protected Button rrButton;
    protected Button ruButton;
    protected Button rdButton;

    private ColorStateList defaultTextColor;

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

        mUdpClient = new UdpClient(SERVER_IP, SERVER_PORT, STATE_SIZE) {
            @Override
            public void onReceive(byte[] data, int size) {
                int value = (0xff & data[0]) * 4 + (0xff & data[1]) / 64;
                double voltage = value * ADC_CONST;
                String batteryState;
                if (voltage < (2 * MIN_VOLTAGE - SMALL_VOLTAGE))
                    batteryState = getApplicationContext().getResources().getString(R.string.state_no_battery);
                else
                    batteryState = String.valueOf((int)((voltage - MIN_VOLTAGE) / (MAX_VOLTAGE - MIN_VOLTAGE) * 100)) + '%';
                mBatteryStateText.setText(batteryState);
                if (voltage <= SMALL_VOLTAGE)
                    mBatteryStateText.setTextColor(Color.RED);
                else
                    mBatteryStateText.setTextColor(defaultTextColor);
            }
        };

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
                    runOnUiThread(() -> mWifiStateText.setText(String.valueOf(currentWifi.getRssi())));
                } else {
                    runOnUiThread(() -> mWifiStateText.setText(R.string.state_no_connection));
                }
            }
        }, 0, WIFI_PERIOD);
    }

    private void initViews() {
        mWifiStateText = findViewById(R.id.controller_signal_state);
        mBatteryStateText = findViewById(R.id.controller_battery_state);
        mSeekBar = findViewById(R.id.controller_seekbar);
        mArmButton = findViewById(R.id.controller_button_u);
        mStopButton = findViewById(R.id.controller_button_d);
        llButton = findViewById(R.id.controller_button_ll);
        lrButton = findViewById(R.id.controller_button_lr);
        rlButton = findViewById(R.id.controller_button_rl);
        rrButton = findViewById(R.id.controller_button_rr);
        ruButton = findViewById(R.id.controller_button_ru);
        rdButton = findViewById(R.id.controller_button_rd);

        mSeekBar.setMax(255);
        mSeekBar.setEnabled(false);

        defaultTextColor = mBatteryStateText.getTextColors();

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
