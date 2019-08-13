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
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApi;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsRegistrationApiLocalImpl;
import ru.vasiliev.sandbox.visionlabs.data.VisionLabsVerifyApi;
import ru.vasiliev.sandbox.visionlabs.domain.VisionLabsConfig;
import ru.vasiliev.sandbox.visionlabs.domain.model.AuthFailReason;
import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResult;
import ru.vasiliev.sandbox.visionlabs.domain.model.SearchResultPerson;
import ru.vasiliev.sandbox.visionlabs.presentation.auth.FaceNotRecognizedFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.SavePhotoFragment;

public class VisionLabsActivity extends MvpAppCompatActivity
        implements VisionLabsView, PhotoFragment.Listener, SavePhotoFragment.Listener,
        FaceNotFoundFragment.Listener, VisionLabsVerifyApi.Listener,
        VisionLabsRegistrationApi.Listener, FaceNotRecognizedFragment.Listener {

    private ProgressDialog mProgress;

    private int mFaceAuthFailsCount;

    @InjectPresenter
    VisionLabsPresenter mPresenter;

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
    protected void onResume() {
        super.onResume();
        mPresenter.init();
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
        PhotoFragment fragment = PhotoFragment.newInstance();
        fragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
        fragment.setListener(this);
        fragment.enableLivenessCheck(false);
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(false);
        showFragment(fragment, PhotoFragment.TAG);
    }

    @Override
    public void showAuth() {
        hideLoader();
        PhotoFragment fragment = PhotoFragment.newInstance();
        fragment.setListener(this);
        fragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
        fragment.enableLivenessCheck(mPresenter.getPreferences().getLivenessAuth());
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(true);
        showFragment(fragment, PhotoFragment.TAG);
    }

    @Override
    public void showPreview() {
        SavePhotoFragment fragment = SavePhotoFragment.newInstance();
        fragment.setPhotoPreview(mPresenter.getFrame());
        fragment.setListener(this);
        showFragment(fragment, SavePhotoFragment.TAG);
    }

    @Override
    public void showFaceNotFound(FaceNotFoundFragment.Reason reason) {
        FaceNotFoundFragment fragment = FaceNotFoundFragment.newInstance();
        fragment.setReason(reason);
        fragment.setListener(this);
        showFragment(fragment, FaceNotFoundFragment.TAG);
    }

    @Override
    public void showFaceNotFoundWarn() {
        Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBestFrameReady(Bitmap frame) {
        mPresenter.onBestFrameReady(frame);
    }

    @Override
    public void onTimeout(FaceNotFoundFragment.Reason reason) {
        mPresenter.onTimeout(reason);

    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onLivenessWaitingOpenedEyes() {

    }

    @Override
    public void onLivenessResult(int state, int action) {

    }

    @Override
    public void onRetryWhenFaceNotRecognized() {
        mPresenter.onRetry();
    }

    @Override
    public void onRetryWhenFaceNotFound() {
        mPresenter.onRetry();
    }

    @Override
    public void onRetryWhenPhotoAccepted() {
        mPresenter.onRetry();
    }

    @Override
    public void onRegisterUser() {
        mPresenter.getRegistrationApi().registerPerson();
    }

    @Override
    public void onRegistrationSuccess() {
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Регистрация прошла успешно")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onRegistrationFail(Throwable throwable) {
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Не удалось выполнить регистрацию")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    @Override
    public void onVerificationSuccess(SearchResult searchResult) {
        mPresenter.setVerificationEndTime(System.nanoTime());
        final List<SearchResultPerson> persons = searchResult.getPersons();
        if (persons != null && !persons.isEmpty()) {
            final SearchResultPerson person = persons.get(0);
            if (person.similarity > VisionLabsConfig.MIN_SIMILARITY) {
                onFaceAuthSuccess();
            } else {
                onFaceAuthFail(AuthFailReason.SIMILARITY);
            }
        } else {
            onFaceAuthFail(AuthFailReason.SIMILARITY);
        }
    }

    @Override
    public void onVerificationFail(Throwable throwable) {
        if (throwable instanceof VisionLabsRegistrationApiLocalImpl.DescriptorNotExtractedException) {
            onFaceAuthFail(AuthFailReason.DESCRIPTOR_EXTRACTION_ERROR);
        } else {
            onFaceAuthFail(AuthFailReason.OTHER);
            throwable.printStackTrace();
        }
    }

    private void onFaceAuthFail(AuthFailReason reason) {
        if (mFaceAuthFailsCount < 4) {
            final FaceNotRecognizedFragment fragment = FaceNotRecognizedFragment.newInstance();
            fragment.setListener(this);
            if (reason == AuthFailReason.SIMILARITY) {
                fragment.setVerificationTime(
                        (int) ((double) (mPresenter.getVerificationEndTime() - mPresenter
                                .getVerificationStartTime()) / 1e6));
            }
            fragment.setFailReason(reason);
            showFragment(fragment, FaceNotRecognizedFragment.TAG);
        } else {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void onFaceAuthSuccess() {
        new AlertDialog.Builder(this).setTitle("Vision Labs")
                .setMessage("Авторизация прошла успешно")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).setCancelable(false).create().show();
    }

    private synchronized void showFragment(Fragment fragment, String tag) {
        if (!popupFragment(tag)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, fragment.toString()).commit();
        }
    }

    private boolean popupFragment(String tag) {
        return getSupportFragmentManager().popBackStackImmediate(tag, 0);
    }
}
