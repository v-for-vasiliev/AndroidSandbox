package ru.vasiliev.sandbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.location.LocationActivity;

/**
 * Date: 29.06.2019
 *
 * @author Kirill Vasiliev
 */
public class MainActivity extends AppCompatActivity {

    @OnClick(R.id.location)
    void onLocationClick() {
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        // intent.putExtra(KEY_PROVIDER_TYPE, PROVIDER_TYPE_FUSED);
        startActivity(intent);
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
