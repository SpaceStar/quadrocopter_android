package com.spacestar.quadrocopter.screen.pid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spacestar.quadrocopter.Consts;
import com.spacestar.quadrocopter.R;
import com.spacestar.quadrocopter.screen.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PidFragment extends BaseFragment {
    private static final String PID_PREFERENCES = "pid_properties";
    private static final String PID_KEY_P = "pid_p";
    private static final String PID_KEY_I = "pid_i";
    private static final String PID_KEY_D = "pid_d";

    @BindView(R.id.pid_p)
    protected TextView mP;
    @BindView(R.id.pid_i)
    protected TextView mI;
    @BindView(R.id.pid_d)
    protected TextView mD;
    @BindView(R.id.pid_p_plus)
    protected View mPPlus;
    @BindView(R.id.pid_i_plus)
    protected View mIPlus;
    @BindView(R.id.pid_d_plus)
    protected View mDPlus;
    @BindView(R.id.pid_p_minus)
    protected View mPMinus;
    @BindView(R.id.pid_i_minus)
    protected View mIMinus;
    @BindView(R.id.pid_d_minus)
    protected View mDMinus;

    private Holder mPValue;
    private Holder mIValue;
    private Holder mDValue;

    private SharedPreferences mPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences =
                getActivity().getSharedPreferences(PID_PREFERENCES, Context.MODE_PRIVATE);
        loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pid, container, false);
        ButterKnife.bind(this, view);
        showData();
        setListeners();
        return view;
    }

    private void loadData() {
        mPValue = new Holder(mPreferences.getFloat(PID_KEY_P, Consts.PID_DEFAULT_P));
        mIValue = new Holder(mPreferences.getFloat(PID_KEY_I, Consts.PID_DEFAULT_I));
        mDValue = new Holder(mPreferences.getFloat(PID_KEY_D, Consts.PID_DEFAULT_D));
    }

    private void showValue(TextView textView, String name, float value) {
        textView.setText(String.format("%s = %s", name, value));
    }

    private void showData() {
        showValue(mP, "P", mPValue.value);
        showValue(mI, "I", mIValue.value);
        showValue(mD, "D", mDValue.value);
    }

    private void setListeners() {
        mPPlus.setOnClickListener(new ValueListener(mP, "P", PID_KEY_P, mPValue, Consts.PID_DELTA_P, true));
        mPMinus.setOnClickListener(new ValueListener(mP, "P", PID_KEY_P, mPValue, Consts.PID_DELTA_P, false));
        mIPlus.setOnClickListener(new ValueListener(mI, "I", PID_KEY_I, mIValue, Consts.PID_DELTA_I, true));
        mIMinus.setOnClickListener(new ValueListener(mI, "I", PID_KEY_I, mIValue, Consts.PID_DELTA_I, false));
        mDPlus.setOnClickListener(new ValueListener(mD, "D", PID_KEY_D, mDValue, Consts.PID_DELTA_D, true));
        mDMinus.setOnClickListener(new ValueListener(mD, "D", PID_KEY_D, mDValue, Consts.PID_DELTA_D, false));
    }

    private class ValueListener implements View.OnClickListener {
        private TextView mTextView;
        private String mName;
        private String mKey;
        private Holder mValue;
        private float mDelta;
        private boolean mPlus;

        public ValueListener(
                TextView textView, String name, String key, Holder value, float delta, boolean plus
        ) {
            mTextView = textView;
            mName = name;
            mKey = key;
            mValue = value;
            mDelta = delta;
            mPlus = plus;
        }

        @Override
        public void onClick(View view) {
            mValue.value = round(mValue.value + mDelta * (mPlus ? 1 : -1));
            mPreferences.edit().putFloat(mKey, mValue.value).apply();
            showValue(mTextView, mName, mValue.value);
        }

        private float round(float value) {
            return ((float)Math.round(value * 100)) / 100;
        }
    }

    public static class Holder {
        public float value;

        public Holder(float value) {
            this.value = value;
        }
    }
}
