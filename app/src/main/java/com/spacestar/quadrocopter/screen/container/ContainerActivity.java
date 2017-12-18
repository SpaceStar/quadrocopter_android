package com.spacestar.quadrocopter.screen.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.spacestar.quadrocopter.R;
import com.spacestar.quadrocopter.screen.base.BaseActivity;
import com.spacestar.quadrocopter.screen.controller.ControllerFragment;
import com.spacestar.quadrocopter.screen.pid.PidFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContainerActivity extends BaseActivity {
    @BindView(R.id.container_modeSpinner)
    protected Spinner mModeSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_container);
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_container, new ControllerFragment())
                .commit();

        mModeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"controller", "pid"}));
        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                Fragment fragment = null;
                switch (index) {
                    case 0:
                        fragment = new ControllerFragment();
                        break;
                    case 1:
                        fragment = new PidFragment();
                        break;
                }
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_container, fragment)
                            .commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }
}
