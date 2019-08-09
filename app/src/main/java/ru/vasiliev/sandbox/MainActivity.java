package ru.vasiliev.sandbox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.location.presentation.LocationActivity;
import ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsActivity;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class MainActivity extends AppCompatActivity {

    @OnClick({R.id.location, R.id.vision_labs})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.location:
                LocationActivity.start(this);
                break;
            case R.id.vision_labs:
                VisionLabsActivity.start(this);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.vasiliev.sandbox.R.layout.activity_main);
        Toolbar toolbar = findViewById(ru.vasiliev.sandbox.R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
    }
}
