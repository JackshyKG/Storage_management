package com.example.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class InfoFragment extends Fragment {

    private int[] images;
    private int currentImage;
    private ImageView ivInfoImages;
    private ImageButton bPrevious, bNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_info, container, false);
        ivInfoImages = rootView.findViewById(R.id.iv_info);
        bPrevious = rootView.findViewById(R.id.b_previous);
        bNext = rootView.findViewById(R.id.b_next);

        init();

        return rootView;

    }

    private void init() {

        images = new int[6];
        currentImage = 0;

        String localLang = Locale.getDefault().getDisplayLanguage();
        if (localLang.equals("русский")) {

            images[0] = R.drawable.info1_ru;
            images[1] = R.drawable.info2_ru;
            images[2] = R.drawable.info3_ru;
            images[3] = R.drawable.info4_ru;
            images[4] = R.drawable.info5_ru;
            images[5] = R.drawable.info6_ru;

            ivInfoImages.setImageResource(R.drawable.info1_ru);

        } else {

            images[0] = R.drawable.info1;
            images[1] = R.drawable.info2;
            images[2] = R.drawable.info3;
            images[3] = R.drawable.info4;
            images[4] = R.drawable.info5;
            images[5] = R.drawable.info6;

        }

        bPrevious.setOnClickListener(v -> {

            if (currentImage < 1) {
                return;
            }

            currentImage --;
            ivInfoImages.setImageResource(images[currentImage]);

        });
        bNext.setOnClickListener(v -> {

            if (currentImage > 4) {
                return;
            }

            currentImage ++;
            ivInfoImages.setImageResource(images[currentImage]);

        });

    }

}
