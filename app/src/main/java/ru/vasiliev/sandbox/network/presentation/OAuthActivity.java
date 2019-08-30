package ru.vasiliev.sandbox.network.presentation;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;

public class OAuthActivity extends MvpAppCompatActivity implements OAuthView {

    @BindView(R.id.phone_edit)
    TextInputEditText mPhoneEdit;

    @BindView(R.id.sms_code_edit)
    TextInputEditText mSmsCodeEdit;

    @InjectPresenter
    OAuthPresenter mOAuthPresenter;

    public static void start(Context context) {
        context.startActivity(new Intent(context, OAuthActivity.class));
    }

    @OnClick({R.id.get_sms_button, R.id.submit_sms_button})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_sms_button:
                mOAuthPresenter.requestSmsCode(mPhoneEdit.getText().toString());
                break;
            case R.id.submit_sms_button:
                mOAuthPresenter.submitSmsCode(mSmsCodeEdit.getText().toString());
            default:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void requestPermissions() {
        RxPermissions.getInstance(this).request(Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        initView();
                    } else {
                        onPermissionsDenied();
                    }
                }, Throwable::printStackTrace);
    }

    @Override
    public void onSmsRequestedSuccessfully() {
        mPhoneEdit.setEnabled(false);
        mSmsCodeEdit.setEnabled(true);
    }

    private void initView() {
        mPhoneEdit.setEnabled(true);
        mSmsCodeEdit.setEnabled(false);
    }

    private void onPermissionsDenied() {
        new AlertDialog.Builder(this).setTitle("Авторизация")
                .setMessage("Необходимо разрешение на чтение состояния телефона")
                .setPositiveButton("OK", (dialogInterface, i) -> finish()).create().show();
    }
}
