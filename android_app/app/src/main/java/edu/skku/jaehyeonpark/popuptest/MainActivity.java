package edu.skku.jaehyeonpark.popuptest;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 3개의 메뉴에 들어갈 Fragment들
    private BookshelfFragment bookshelfFragment = new BookshelfFragment();
    private WordbookFragment wordbookFragment = new WordbookFragment();
    private SettingFragment settingFragment = new SettingFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_bookshelf: {
                    transaction.replace(R.id.frame_layout, bookshelfFragment).commitAllowingStateLoss();
                    break;
                }
                case R.id.navigation_wordbook: {
                    transaction.replace(R.id.frame_layout, wordbookFragment).commitAllowingStateLoss();
                    break;
                }
                case R.id.navigation_setting: {
                    transaction.replace(R.id.frame_layout, settingFragment).commitAllowingStateLoss();
                    break;
                }
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            Log.d("jh park", "MainActivity savedInstace is not null");
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, bookshelfFragment).commitAllowingStateLoss();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
