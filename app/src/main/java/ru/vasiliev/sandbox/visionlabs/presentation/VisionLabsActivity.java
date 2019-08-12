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
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
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

import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.AUTH;
import static ru.vasiliev.sandbox.visionlabs.presentation.VisionLabsPresenter.Mode.REGISTRATION;

public class VisionLabsActivity extends MvpAppCompatActivity
        implements VisionLabsView, PhotoFragment.Listener, SavePhotoFragment.Listener,
        FaceNotFoundFragment.Listener, VisionLabsVerifyApi.Listener,
        VisionLabsRegistrationApi.Listener, FaceNotRecognizedFragment.Listener {

    @BindView(R.id.output)
    TextView mOutput;

    private ProgressDialog mProgress;

    private Bitmap mBitmap;

    private int mFaceAuthFailsCount;

    private long verifStartTime;

    private long verifEndTime;

    private PhotoFragment mPhotoFragment;

    private FaceNotFoundFragment mFaceNotFoundFragment;

    private SavePhotoFragment mSavePhotoFragment;

    private FaceNotRecognizedFragment mFaceNotRecognizedFragment;

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
        mPresenter.getRegistrationApi().setListener(this);
        mPresenter.getVerifyApi().setListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.getRegistrationApi().setListener(null);
        mPresenter.getVerifyApi().setListener(null);
    }

    @Override
    public void onBackPressed() {
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
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
                } else {
                    mPresenter.onEngineLoadedSucceeded();
                }
            } else {
                onEngineLoadError();
            }
        });
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
    public void showRegistration() {
        hideLoader();
        if (mPhotoFragment == null) {
            mPhotoFragment = PhotoFragment.newInstance();
        }
        mPhotoFragment.setListener(this);
        mPhotoFragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
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
        }

        mPhotoFragment.setListener(this);
        mPhotoFragment.setPhotoProcessor(mPresenter.getPhotoProcessor());
        mPhotoFragment.enableLivenessCheck(mPresenter.getPreferences().getLivenessAuth());

        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(true);

        showFragment(mPhotoFragment, PhotoFragment.TAG);
    }

    @Override
    public void onBestFrameReady(Bitmap bitmap) {
        if (mPresenter.getMode() == REGISTRATION) {
            mBitmap = bitmap;
            if (mSavePhotoFragment == null) {
                mSavePhotoFragment = SavePhotoFragment.newInstance();
            }
            mSavePhotoFragment.setPhotoPreview(mBitmap);
            mSavePhotoFragment.setListener(this);
            showFragment(mSavePhotoFragment, SavePhotoFragment.TAG);
        } else if (mPresenter.getMode() == AUTH) {
            verifStartTime = System.nanoTime();
            mPresenter.getVerifyApi().verifyPerson();
        }
    }

    @Override
    public void onTimeout(FaceNotFoundFragment.Reason reason) {
        if (mPresenter.getMode() == REGISTRATION) {
            if (mFaceNotFoundFragment == null) {
                mFaceNotFoundFragment = FaceNotFoundFragment.newInstance();
            }
            mFaceNotFoundFragment.setReason(reason);
            mFaceNotFoundFragment.setListener(this);
            showFragment(mFaceNotFoundFragment, FaceNotFoundFragment.TAG);
        } else if (mPresenter.getMode() == AUTH) {
            mFaceAuthFailsCount++;
            if (mFaceAuthFailsCount < 5) {
                if (mFaceNotFoundFragment == null) {
                    mFaceNotFoundFragment = FaceNotFoundFragment.newInstance();
                }
                mFaceNotFoundFragment.setReason(reason);
                mFaceNotFoundFragment.setListener(this);
                showFragment(mFaceNotFoundFragment, FaceNotFoundFragment.TAG);
            } else {
                Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            }
        }
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
        popupFragment(PhotoFragment.TAG);
    }

    @Override
    public void onRetryWhenFaceNotFound() {
        popupFragment(PhotoFragment.TAG);
    }

    @Override
    public void onRetryWhenPhotoAccepted() {
        popupFragment(PhotoFragment.TAG);
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
        verifEndTime = System.nanoTime();
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
                        (int) ((double) (verifEndTime - verifStartTime) / 1e6));
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
                    .replace(R.id.container, fragment, fragment.toString()).addToBackStack(tag)
                    .commit();
        }
    }

    private boolean popupFragment(String tag) {
        return getSupportFragmentManager().popBackStackImmediate(tag, 0);
    }
}
