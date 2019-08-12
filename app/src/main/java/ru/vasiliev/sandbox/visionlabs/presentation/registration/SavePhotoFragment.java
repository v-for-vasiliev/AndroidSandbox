package ru.vasiliev.sandbox.visionlabs.presentation.registration;


import com.makeramen.roundedimageview.RoundedImageView;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.vasiliev.sandbox.R;

public class SavePhotoFragment extends Fragment {

    @BindView(R.id.photo)
    RoundedImageView photo;

    Bitmap bitmap;

    Listener listener;

    @BindView(R.id.save)
    Button save;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    public SavePhotoFragment() {

    }

    public static SavePhotoFragment newInstance() {
        return new SavePhotoFragment();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPhoto(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_photo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photo.setImageBitmap(bitmap);
        bitmap = null;
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.accentColor),
                        PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public String toString() {
        return "Save Photo Fragment";
    }

    @OnClick({R.id.retry})
    public void onRetryClick() {
        if (listener != null) {
            listener.onRetryClick();
        }
    }

    @OnClick({R.id.save})
    public void onClick() {
        save.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        if (listener != null) {
            listener.onSaveClick();
        }
    }

    public interface Listener {

        void onRetryClick();

        void onSaveClick();
    }

}
