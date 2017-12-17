package com.spacestar.quadrocopter.screen.container;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.spacestar.quadrocopter.R;
import com.spacestar.quadrocopter.screen.base.BaseActivity;
import com.spacestar.quadrocopter.screen.controller.ControllerFragment;

public class ContainerActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_container, new ControllerFragment())
                .commit();
    }
}
