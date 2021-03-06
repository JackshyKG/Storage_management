package com.example.management;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.management.Reports.ReportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;


public class MainActivity extends AppCompatActivity  {

    private int clickedButtonId;
    private Fragment activeFragment;
    private DocFragment docFragment;
    private ItemFragment itemFragment;
    private ReportsFragment reportsFragment;
    private InfoFragment infoFragment;
    private FragmentManager supportFM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {

        clickedButtonId = R.id.b_nav_doc;
        docFragment = new DocFragment();
        itemFragment = new ItemFragment();
        reportsFragment = new ReportsFragment();
        infoFragment = new InfoFragment();
        activeFragment = docFragment;

        supportFM = getSupportFragmentManager();
        supportFM.beginTransaction().add(R.id.main_fragment, docFragment).
                                     add(R.id.main_fragment, itemFragment).hide(itemFragment).
                                     add(R.id.main_fragment, reportsFragment).hide(reportsFragment).
                                     add(R.id.main_fragment, infoFragment).hide(infoFragment).commit();

        // bottom navigation on item selected listener
        BottomNavigationView bnvMain = findViewById(R.id.bnv_main);
        bnvMain.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == clickedButtonId) {
                return false;/* Last nav button was clicked again, nothing to change */
            }

            clickedButtonId = item.getItemId();

            switch (clickedButtonId) {

                case R.id.b_nav_doc:
                    supportFM.beginTransaction().hide(activeFragment).show(docFragment).commit();
                    activeFragment = docFragment;
                    break;

                case R.id.b_nav_item:
                    supportFM.beginTransaction().hide(activeFragment).show(itemFragment).commit();
                    activeFragment = itemFragment;
                    break;

                case R.id.b_nav_report:
                    supportFM.beginTransaction().hide(activeFragment).show(reportsFragment).commit();
                    activeFragment = reportsFragment;
                    break;

                case R.id.b_nav_info:
                    supportFM.beginTransaction().hide(activeFragment).show(infoFragment).commit();
                    activeFragment = infoFragment;
                    break;

            }

            return true;

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
