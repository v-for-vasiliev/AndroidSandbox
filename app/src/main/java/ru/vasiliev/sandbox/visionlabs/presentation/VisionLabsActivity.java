package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vasiliev.sandbox.App;
import ru.vasiliev.sandbox.R;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.FaceNotFoundFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.common.PhotoFragment;
import ru.vasiliev.sandbox.visionlabs.presentation.registration.SavePhotoFragment;

public class VisionLabsActivity extends MvpAppCompatActivity
        implements VisionLabsView, PhotoFragment.Listener, SavePhotoFragment.Listener,
        FaceNotFoundFragment.Listener {

    @BindView(R.id.output)
    TextView mOutput;

    private ProgressDialog mProgress;

    private Bitmap mBitmap;

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
    public void onBackPressed() {
        Log.i("STATUS", "STACK COUNT " + getSupportFragmentManager().getBackStackEntryCount());
        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
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
                onEngineLoadFinished(false);
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
    public void onEngineLoadFinished(boolean result) {
        mProgress.dismiss();
        mOutput.setText("Engine load status: " + result);
        if (result) {
            showPhotoScreen();
        }
    }

    @Override
    public void onEngineLoadError(Throwable t) {
        mProgress.dismiss();
        mOutput.setText("Error load engine: " + t.getMessage());
    }

    private void showPhotoScreen() {
        final PhotoFragment fragment = PhotoFragment.newInstance();
        fragment.setPhotoProcessor(
                App.getComponentManager().getVisionLabsComponent().getPhotoProcessor());
        fragment.setListener(this);
        fragment.enableLivenessCheck(false);

        mPresenter.getPreferences().setStartTime(String.valueOf(System.currentTimeMillis()));
        mPresenter.getPreferences().setNeedPortrait(false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PhotoFragment.class.getName())
                .addToBackStack(fragment.toString()).commit();
    }

    private void showPhotoReadyScreen(Bitmap bitmap) {
        final SavePhotoFragment fragment = SavePhotoFragment.newInstance();
        fragment.setPhoto(bitmap);
        fragment.setListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, SavePhotoFragment.class.getName())
                .addToBackStack(fragment.toString()).commit();
    }

    @Override
    public void onBestFrameReady(Bitmap bitmap) {
        this.mBitmap = bitmap;
        showPhotoReadyScreen(bitmap);
    }

    @Override
    public void onTimeout(FaceNotFoundFragment.Reason reason) {
        final FaceNotFoundFragment fragment = FaceNotFoundFragment.newInstance();
        fragment.setReason(reason);
        fragment.setListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FaceNotFoundFragment.class.getName())
                .addToBackStack(fragment.toString()).commit();
    }

    @Override
    public void onNeedCameraPermission() {

    }

    @Override
    public void onLivenessWaitingOpenedEyes() {

    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onLivenessResult(int state, int action) {

    }

    @Override
    public void onRetryClick() {
        onBackPressed();
    }

    @Override
    public void onSaveClick() {

    }
}
