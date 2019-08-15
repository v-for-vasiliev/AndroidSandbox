package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.visionlabs.domain.model.AuthFailReason;
import ru.vasiliev.sandbox.visionlabs.presentation.auth.FaceNotRecognizedFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.SavePhotoFragment;

public class VisionLabsActivity extends MvpAppCompatActivity implements VisionLabsView {

    private ProgressDialog mProgress;

    @InjectPresenter
    VisionLabsPresenter mPresenter;

    private PhotoFragment mPhotoFragment;

    @ProvidePresenter
    VisionLabsPresenter providePresenter() {
        return new VisionLabsPresenter(this);
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, VisionLabsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visionlabs);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Загрузка...");
        mProgress.setCancelable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.releaseComponent();
    }

    @Override
    public void onBackPressed() {
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            popupFragment(PhotoFragment.TAG);
        } else {
            finish();
        }
    }

    @Override
    public void requestPermissions() {
        RxPermissions.getInstance(this).request(Manifest.permission.CAMERA).subscribe(granted -> {
            if (granted) {
                if (Camera.getNumberOfCameras() == 1) {
                    mPresenter.getPreferences().setUseFrontCamera(false);
                }
                if (!mPresenter.isEngineLoaded()) {
                    mPresenter.loadEngine();
                }
            } else {
                onEngineLoadError();
            }
        });
    }

    @Override
    public void onEngineLoadError() {
        hideLoader();
        mProgress.dismiss();
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Ошибка инициализации Vision Labs")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void showLoader() {
        if (!mProgress.isShowing()) {
            mProgress.show();
        }
    }

    @Override
    public void hideLoader() {
        mProgress.dismiss();
    }

    @Override
    public void showRegistration() {
        hideLoader();
        if (mPhotoFragment == null) {
            mPhotoFragment = PhotoFragment.newInstance();
        } else {
            mPhotoFragment.hideWaitState();
        }
        mPhotoFragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
        mPhotoFragment.setListener(mPresenter);
        mPhotoFragment.enableLivenessCheck(false);
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(false);
        showFragment(mPhotoFragment, PhotoFragment.TAG);
    }

    @Override
    public void showAuth() {
        hideLoader();
        if (mPhotoFragment == null) {
            mPhotoFragment = PhotoFragment.newInstance();
        } else {
            mPhotoFragment.hideWaitState();
        }
        mPhotoFragment.setListener(mPresenter);
        mPhotoFragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
        mPhotoFragment.enableLivenessCheck(mPresenter.getPreferences().getLivenessAuth());
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(true);
        showFragment(mPhotoFragment, PhotoFragment.TAG);
    }

    @Override
    public void showPreview() {
        SavePhotoFragment fragment = SavePhotoFragment.newInstance();
        fragment.setPhotoPreview(mPresenter.getFrame());
        fragment.setListener(mPresenter);
        showFragment(fragment, SavePhotoFragment.TAG);
    }

    @Override
    public void showFaceNotFound(FaceNotFoundFragment.Reason reason) {
        FaceNotFoundFragment fragment = FaceNotFoundFragment.newInstance();
        fragment.setReason(reason);
        fragment.setListener(mPresenter);
        showFragment(fragment, FaceNotFoundFragment.TAG);
    }

    @Override
    public void showFaceNotFoundWarn() {
        Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRegistrationSucceeded() {
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Регистрация прошла успешно")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onRegistrationFailed(Throwable throwable) {
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Не удалось выполнить регистрацию")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onFaceAuthSucceeded() {
        mPhotoFragment.showWaitState();
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Авторизация прошла успешно")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onFaceFailedAttempt() {
        mPhotoFragment.showWaitState();
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Отказано в доступе, попробуйте еще раз")
                .setPositiveButton("OK", (dialog, which) -> {
                    showAuth();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onFaceAuthFailed(AuthFailReason reason, int verificationTimeMs) {
        FaceNotRecognizedFragment fragment = FaceNotRecognizedFragment.newInstance();
        fragment.setListener(mPresenter);
        if (verificationTimeMs > 0) {
            fragment.setVerificationTime(verificationTimeMs);
        }
        fragment.setFailReason(reason);
        showFragment(fragment, FaceNotRecognizedFragment.TAG);
    }

    private synchronized void showFragment(Fragment fragment, String tag) {
        if (!popupFragment(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, fragment.toString()).addToBackStack(tag)
                    .commit();
        }
    }

    private boolean popupFragment(String tag) {
        return getSupportFragmentManager().popBackStackImmediate(tag, 0);
    }
}
