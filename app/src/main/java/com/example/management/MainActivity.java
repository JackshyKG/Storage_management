package com.example.management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.management.Reports.ReportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity  {

    private int clickedButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        openReportFragment();//openDocFragment

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
                    openDocFragment();
                    break;

                case R.id.b_nav_item:
                    openItemFragment();
                    break;

                case R.id.b_nav_report:
                    openReportFragment();
                    break;

            }

            return true;

        });

    }

    private void openDocFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new DocFragment()).commit();
    }

    private void openItemFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new ItemFragment()).commit();
    }

    private void openReportFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new ReportsFragment()).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
