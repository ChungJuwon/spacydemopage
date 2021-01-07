package edu.skku.jaehyeonpark.popuptest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class ReaderActivity extends AppCompatActivity {
    private String TAG = "jh park";
    private ConstraintLayout mConstaraint;
    private Button mButton, mBtnPrev, mBtnNext, mBtnNight;
    private ReaderOnTouchListener readerOnTouchListener;
    private PageHandler pageHandler;
    TextView mTextView;
    String mFileName = null;
    MyViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        mTextView = findViewById(R.id.textView);

        mViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        mFileName = getIntent().getStringExtra("fileName");
        if(mFileName == null){
            finish();
        }
        String content = mViewModel.getContent(this, mFileName);
        pageHandler = new PageHandler(content, mTextView);
        pageHandler.pageStartOffset = getPreferences(Context.MODE_PRIVATE).getInt(mFileName, 0);

        mTextView.post(new Runnable() {
            @Override
            public void run() {
                pageHandler.refreshPage();
            }
        });

        readerOnTouchListener = new ReaderOnTouchListener(this, mTextView);
        mTextView.setOnTouchListener(readerOnTouchListener);
        readerOnTouchListener.setOnSwipeLisner(new ReaderOnTouchListener.OnSwipeListener() {
            @Override
            public boolean onRightToLeft() {
                pageHandler.nextPage();
                return true;
            }

            @Override
            public boolean onLeftToRight() {
                pageHandler.prevPage();
                return false;
            }
        });

//        mBtnNight = findViewById(R.id.btnNight);
//        mBtnNight.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleNightMode();
//            }
//        });
//
//        mBtnPrev = findViewById(R.id.btnPrev);
//        mBtnPrev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pageHandler.prevPage();
//            }
//        });
//
//        mBtnNext = findViewById(R.id.btnNext);
//        mBtnNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pageHandler.nextPage();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConstaraint = findViewById(R.id.constraintLayout);
        mConstaraint.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mFileName != null && pageHandler != null) {
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(mFileName, pageHandler.pageStartOffset);
            editor.commit();

            saveLog();
        }
    }

    private void toggleNightMode() {
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        ReaderActivity.this.recreate();
    }

    private void saveLog(){
        try{
            FileWriter fw = new FileWriter(new File(getFilesDir(), "log.txt"));
//            fw.append(readerOnTouchListener.getLog());
            fw.flush();
            fw.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
