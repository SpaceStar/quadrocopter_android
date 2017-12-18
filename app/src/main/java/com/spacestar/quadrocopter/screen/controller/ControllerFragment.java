package com.spacestar.quadrocopter.screen.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spacestar.quadrocopter.R;
import com.spacestar.quadrocopter.UdpClient;
import com.spacestar.quadrocopter.screen.base.BaseFragment;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.spacestar.quadrocopter.Consts.*;

public class ControllerFragment extends BaseFragment {

    @BindView(R.id.controller_signal_state)
    protected TextView mWifiStateText;
    @BindView(R.id.controller_battery_state)
    protected TextView mBatteryStateText;
    @BindView(R.id.controller_seekbar)
    protected SeekBar mSeekBar;
    @BindView(R.id.controller_button_arm)
    protected Button mArmButton;
    @BindView(R.id.controller_button_stop)
    protected Button mStopButton;
    @BindView(R.id.controller_button_ll)
    protected Button mLlButton;
    @BindView(R.id.controller_button_lr)
    protected Button mLrButton;
    @BindView(R.id.controller_button_rl)
    protected Button mRlButton;
    @BindView(R.id.controller_button_rr)
    protected Button mRrButton;
    @BindView(R.id.controller_button_ru)
    protected Button mRuButton;
    @BindView(R.id.controller_button_rd)
    protected Button mRdButton;

    byte state[] = new byte[STATE_SIZE];
    private UdpClient mUdpClient;
    private Timer mUdpTimer;
    private Timer mWifiTimer;
    private WifiManager mWifiManager;
    private boolean mWifiDisable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        ButterKnife.bind(this, view);
        initViews();
        initWifi();
        initClient();
        return view;
    }

    private void initClient() {
        mUdpClient = new UdpClient(SERVER_IP, SERVER_PORT, STATE_SIZE) {
            @Override
            public void onReceive(byte[] data, int size) {
                int value = (0xff & data[0]) * 4 + (0xff & data[1]) / 64;
                double voltage = value * ADC_CONST;
                String batteryState;
                if (voltage < (2 * MIN_VOLTAGE - SMALL_VOLTAGE))
                    batteryState = getContext().getResources().getString(R.string.state_no_battery);
                else
                    batteryState = String.valueOf((int) ((voltage - MIN_VOLTAGE) / (MAX_VOLTAGE - MIN_VOLTAGE) * 100)) + '%';
                mBatteryStateText.setText(batteryState);
                if (voltage <= SMALL_VOLTAGE) {
                    mBatteryStateText.setTextColor(Color.RED);
                } else {
                    mBatteryStateText.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color));
                }
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
                if ((currentWifi.getNetworkId() != -1) && (currentWifi.getSSID().equals("\"" + getContext().getResources().getString(R.string.wifi_ssid) + "\""))) {
                    getActivity().runOnUiThread(() -> mWifiStateText.setText(String.valueOf(currentWifi.getRssi())));
                } else {
                    getActivity().runOnUiThread(() -> mWifiStateText.setText(R.string.state_no_connection));
                }
            }
        }, 0, WIFI_PERIOD);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        mSeekBar.setMax(255);
        mSeekBar.setEnabled(false);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                state[Channel.GAS.ordinal()] = (byte) progress;
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
                state[Channel.OPTIONS.ordinal()] ^= 1;

                if ((state[Channel.OPTIONS.ordinal()] & 1) == 1) {
                    mArmButton.setText(R.string.arm_off);
                    mArmButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_light));
                    mSeekBar.setEnabled(true);
                } else {
                    mArmButton.setText(R.string.arm_on);
                    mArmButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                    mSeekBar.setEnabled(false);
                }
            }

            return true;
        });

        mStopButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mSeekBar.setProgress(0);

                state[Channel.OPTIONS.ordinal()] &= ~1;
                mArmButton.setText(R.string.arm_on);
                mArmButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_light));
                mSeekBar.setEnabled(false);
            }
            return true;
        });

        View.OnTouchListener onTouchListener = (v, event) -> {
            Button view = (Button) v;
            Channel ch;
            byte value;

            switch (view.getId()) {
                case R.id.controller_button_ll:
                    ch = Channel.YAW;
                    value = -128;
                    break;
                case R.id.controller_button_lr:
                    ch = Channel.YAW;
                    value = 127;
                    break;
                case R.id.controller_button_rl:
                    ch = Channel.ROLL;
                    value = -128;
                    break;
                case R.id.controller_button_rr:
                    ch = Channel.ROLL;
                    value = 127;
                    break;
                case R.id.controller_button_ru:
                    ch = Channel.PITCH;
                    value = 127;
                    break;
                case R.id.controller_button_rd:
                    ch = Channel.PITCH;
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

        mLlButton.setOnTouchListener(onTouchListener);
        mLrButton.setOnTouchListener(onTouchListener);
        mRlButton.setOnTouchListener(onTouchListener);
        mRrButton.setOnTouchListener(onTouchListener);
        mRuButton.setOnTouchListener(onTouchListener);
        mRdButton.setOnTouchListener(onTouchListener);
    }

    private void initWifi() {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + getContext().getResources().getString(R.string.wifi_ssid) + "\"";
        conf.preSharedKey = "\"" + getContext().getResources().getString(R.string.wifi_pass) + "\"";

        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
    public void onDestroy() {
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
