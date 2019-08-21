package ru.vasiliev.sandbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.location.presentation.LocationActivity;
import ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsActivity;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class MainActivity extends AppCompatActivity {

    @OnClick({R.id.location, R.id.vision_labs, R.id.app_settings})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_settings:
                openAppSettings();
                break;
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

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
