package ru.vasiliev.sandbox.sovestoauth.presentation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;
import rx.subscriptions.CompositeSubscription;

public class OAuthActivity extends AppCompatActivity {

    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    public static void start(Context context) {
        context.startActivity(new Intent(context, OAuthActivity.class));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.clear();
    }
}
