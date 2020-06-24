package com.example.management;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity  {

    private int clickedButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        createAndOpenDocFragment();

    }

    private void init() {

        clickedButtonId = R.id.b_nav_doc;

        // bottom navigation on item selected listener
        BottomNavigationView bnvMain = findViewById(R.id.bnv_main);
        bnvMain.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == clickedButtonId) {
                return false;// Повторна нажата та же кнопка, что и в последний раз
            }

            clickedButtonId = item.getItemId();

            switch (clickedButtonId) {

                case R.id.b_nav_doc:
                    createAndOpenDocFragment();
                    break;

                case R.id.b_nav_item:
                    createAndOpenItemFragment();
                    break;

                case R.id.b_nav_report:

                    break;

            }

            return true;

        });

    }

    private void createAndOpenDocFragment() {
        DocFragment selectedFragment = new DocFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, selectedFragment).commit();
    }

    private void createAndOpenItemFragment() {
        DocFragment selectedFragment = new DocFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, selectedFragment).commit();
    }

}
