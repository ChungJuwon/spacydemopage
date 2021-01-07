package edu.skku.jaehyeonpark.popuptest;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;

public class ReaderOnTouchListener implements View.OnTouchListener {
    String TAG = "jh park";
    private PopupWindow mPopupWindow;
    private TextView tvTranslate;
    private Context mContext;
    private TextView mTextView;
    private AnimatorSet animSet;
    private final int PADDING;
    private int mMinPopupHeight;
    private OnSwipeListener mOnSwipeListener;
    String url = "http://192.168.0.2:5000/MWE-dict";

    interface OnSwipeListener {
        public boolean onRightToLeft();
        public boolean onLeftToRight();
    }

    public void setOnSwipeLisner(OnSwipeListener onSwipeListener){
        mOnSwipeListener = onSwipeListener;
    }

    GestureDetector mDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
        String word;
        int sentStart;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown: ");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress: ");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp: ");
            if(mPopupWindow.isShowing()){
                mPopupWindow.dismiss();
                clearSpan();
                return true;
            }

            int[] loc = new int[3]; // loc : coordinates of start, end, width
            int offset = getValidOffset(e);
            String word = getWordRect(offset, loc);

            tvTranslate.setText(word);
            mPopupWindow.dismiss();
            mPopupWindow.getContentView().setMinimumWidth(loc[2]);
            mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, loc[0], loc[1]);

            //translator.doTranslate(word, true);
            try {
                translate(offset);
            }catch (JSONException ex){
                ex.printStackTrace();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: ");

            if(velocityY > 2000 || velocityY < -2000){
                return true;
            }

            if(velocityX < -500) {
                return mOnSwipeListener.onRightToLeft();
            }else if(velocityX > 500){
                return mOnSwipeListener.onLeftToRight();
            }

            return true;
        }

        private int getValidOffset(MotionEvent e){
            int offset = mTextView.getOffsetForPosition(e.getX(), e.getY());
            BreakIterator boundary = BreakIterator.getWordInstance();
            boundary.setText(mTextView.getText().toString());

            while(boundary.isBoundary(offset) && offset >= 0){ offset--; }
            while(boundary.isBoundary(offset)){ offset++; }

            return offset;
        }

        private void translate(int offset) throws JSONException {
            String text = mTextView.getText().toString();
            BreakIterator boundary = BreakIterator.getSentenceInstance();
            boundary.setText(text);
            int sentEnd = boundary.following(offset);
            sentStart= boundary.previous();
            String sent = text.substring(sentStart, sentEnd);

            JSONObject reqBody = new JSONObject();
            reqBody.put("sent", sent);
            reqBody.put("sel_offset", offset-sentStart);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, reqBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Gson gson = new Gson();
                            MWEResult mweResult = gson.fromJson(response.toString(), MWEResult.class);
                            tvTranslate.setText(mweResult.getMWE() + " : " + mweResult.getMeaning());
                            Spannable espan = (Spannable)mTextView.getText();
                            for(int []i : mweResult.getOffsets()){
                                espan.setSpan(new BackgroundColorSpan(mContext.getColor(R.color.colorTextHighlight)),
                                        sentStart +i[0], sentStart +i[1], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            tvTranslate.setText(error.toString());
                        }
                    }
            );

            MySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }

        private void clearSpan(){
            Spannable espan = (Spannable)mTextView.getText();
            BackgroundColorSpan[] spans = espan.getSpans(0, espan.length(), BackgroundColorSpan.class);
            for(BackgroundColorSpan s : spans){
                espan.removeSpan(s);
            }
        }

    }); //GestureDetector mGestureDetector


    public ReaderOnTouchListener(Context context, TextView textView) {
        mContext = context;
        this.mTextView = textView;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.custom_layout, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.popup_window_animation);

        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        mDetector.setIsLongpressEnabled(false);

        PADDING = mPopupWindow.getContentView().getPaddingLeft() * 2;
        tvTranslate = (TextView) mPopupWindow.getContentView().findViewById(R.id.tvTranslate);

        mTextView.post(new Runnable() {
            @Override
            public void run() {
                tvTranslate.setText("-");
                Layout layout = mTextView.getLayout();
                mMinPopupHeight = (int)((layout.getLineBaseline(0) - layout.getLineAscent(0)) * 0.7);
                tvTranslate.setMinHeight(mMinPopupHeight);
                mPopupWindow.showAsDropDown(mTextView);
                tvTranslate.post(new Runnable() {
                    @Override
                    public void run() {
                        mPopupWindow.dismiss();
                    }
                });
            }
        });

        //translator = new Translator(tvTranslate);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private String getWordRect(int offset, int[] location) {
        int x, y, width, height;
        int start, end;
        String text = mTextView.getText().toString();

        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);

        if(boundary.isBoundary(offset)) throw new RuntimeException("offset should not be boundary");

        end = boundary.following(offset);
        start = boundary.previous();

        Layout layout = mTextView.getLayout();
        int line = layout.getLineForOffset(offset);
        float horizon = layout.getPrimaryHorizontal(start);
        float horizon2 = layout.getPrimaryHorizontal(end);
        int vertical = layout.getLineTop(line) - mMinPopupHeight + 5;

        int[] loc = new int[2];
        mTextView.getLocationInWindow(loc);
        x = loc[0] + (int) horizon;
        y = loc[1] + vertical; //- mPopupWindow.getContentView().getHeight();
        width = (int) (horizon2 - horizon) + PADDING;

        location[0] = x;
        location[1] = y;
        location[2] = width;

//        Log.d(TAG, "getWordRect: ." + str.substring(startIndex, endIndex));

        return text.substring(start, end);
    }

    public void setTextView(TextView mTextView) {
        this.mTextView = mTextView;
    }

//    public String getLog(){
//        return translator.stringBuilder.toString();
//    }
}
