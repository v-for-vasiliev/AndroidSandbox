package ru.vasiliev.sandbox.visionlabs.presentation;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vasiliev.sandbox.R;

public class VisionLabsActivity extends MvpAppCompatActivity implements VisionLabsView {

    @BindView(R.id.output)
    TextView mOutput;

    private ProgressDialog mProgress;

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
    public void showLoader() {
        if (!mProgress.isShowing()) {
            mProgress.show();
        }
    }

    @Override
    public void onEngineLoadFinished(boolean result) {
        mProgress.dismiss();
        mOutput.setText("Engine load status: " + result);
    }

    @Override
    public void onEngineLoadError(Throwable t) {
        mProgress.dismiss();
        mOutput.setText("Error load engine: " + t.getMessage());
    }
}
