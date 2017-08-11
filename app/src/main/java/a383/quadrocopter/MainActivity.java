package a383.quadrocopter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final int MINIMUM = 3;
    public static final int MAXIMUM = 100;
    public static final long PERIOD = 50;

    protected Button mOnOffButton;
    protected SeekBar mSeekBar;
    protected TextView mNumberText;

    private boolean mIsOn;
    private TcpClient mTcpClient;

    private Timer mSignalTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        mTcpClient = new TcpClient(message -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
        new Thread(() -> mTcpClient.run()).start();

        mSignalTimer = new Timer();
        mSignalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mIsOn) {
                    mTcpClient.sendMessage(String.valueOf(mSeekBar.getProgress() + MINIMUM));
                } else {
                    mTcpClient.sendMessage("0");
                }
            }
        }, 0, PERIOD);
    }

    private void initViews() {
        mOnOffButton = ((Button) findViewById(R.id.main_startButton));
        mSeekBar = ((SeekBar) findViewById(R.id.main_seekBar));
        mNumberText = ((TextView) findViewById(R.id.main_number));

        mOnOffButton.setOnClickListener(v -> {
            mIsOn = !mIsOn;

            if (mIsOn) {
                mOnOffButton.setText(R.string.main_off);
            } else {
                mOnOffButton.setText(R.string.main_on);
            }
        });

        mSeekBar.setMax(MAXIMUM - MINIMUM);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mNumberText.setText(String.valueOf(i + MINIMUM));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
        if (mSignalTimer != null) {
            mSignalTimer.cancel();
        }
    }
}
