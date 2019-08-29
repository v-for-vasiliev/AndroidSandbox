package ru.vasiliev.sandbox.sovestoauth.presentation;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;

public class OAuthActivity extends MvpAppCompatActivity {

    @BindView(R.id.phone_edit)
    TextInputEditText mPhoneEdit;

    @BindView(R.id.sms_code_edit)
    TextInputEditText mSmsCodeEdit;

    @BindView(R.id.next_button)
    Button mNextButton;

    @InjectPresenter
    OAuthPresenter mOAuthPresenter;

    public static void start(Context context) {
        context.startActivity(new Intent(context, OAuthActivity.class));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        if (mOAuthPresenter.isSmsCodeRequested()) {
            mPhoneEdit.setEnabled(false);
            mSmsCodeEdit.setEnabled(true);
        } else {
            mPhoneEdit.setEnabled(true);
            mSmsCodeEdit.setEnabled(false);
        }
    }
}
