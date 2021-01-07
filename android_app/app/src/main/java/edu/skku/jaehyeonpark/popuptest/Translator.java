package edu.skku.jaehyeonpark.popuptest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Translator {
//    String TAG = "jh park";
//    private static Translate translate = null;
//    public StringBuilder stringBuilder = new StringBuilder();
//    private OnResultListener onResultListener = null;
//
//    public Translator(TextView textView) {
//        mTextView = textView;
//        init();
//    }
//
//    public interface OnResultListener{
//        void onResult();
//        void onFail();
//    }
//
//    public void setOnResultListener(OnResultListener onResultListener){
//        this.onResultListener = onResultListener;
//    }
//
//    public void init() {
//        Context context = mTextView.getContext();
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//        if (networkInfo == null) {
//            Log.d(TAG, "Translator: network fail");
//            onResultListener.onFail();
//            return;
//        }
//
//
//    }
//
//    public void doTranslate(String word, boolean showOriginal) {
//        Log.d(TAG, "doTranslate: ");
//
//        stringBuilder.append(word + ";\n");
//
//        if (showOriginal) mTextView.setText(word);
//
//        if (translate == null) {
//            Log.d(TAG, "doTranslate: translate is null");
//            return;
//        }
//        MyAsyncTask asyncTask = new MyAsyncTask(mTextView);
//        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, word);
//    }
//
//
//    private static class MyAsyncTask extends AsyncTask<String, String, String> {
//        private WeakReference<TextView> textViewReference;
//        private Translate translate = Translator.translate;
//
//        MyAsyncTask(TextView textView) {
//            textViewReference = new WeakReference<>(textView);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//        }
//
//        @Override
//        protected void onProgressUpdate(String... strings) {
//            super.onProgressUpdate(strings);
//            TextView textView = textViewReference.get();
//            String translatedText = strings[0];
//            if(textView.isShown())
//                textView.setText(translatedText);
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            Log.d("jh2", "doInBackground: ");
//            String originalText = strings[0];
//            String translatedText = "-";
//            try {
//                translatedText = translate(originalText);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                publishProgress(translatedText, originalText);
//            }
//            return null;
//        }
//
//        public String translate(String originalText) {
//            Log.d("jh2", "translate: " + originalText);
//            Translation translation = translate.translate(originalText, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("ko"));
//            String translatedText = translation.getTranslatedText();
//
//            return translatedText;
//        }
//    }
//
//    private static class InitAsyncTask extends AsyncTask<Void, Translate, String> {
//        private WeakReference<TextView> textViewReference;
//
//        InitAsyncTask(TextView textView) {
//            textViewReference = new WeakReference<>(textView);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//        }
//
//        @Override
//        protected void onProgressUpdate(Translate... translates) {
//            super.onProgressUpdate(translates[0]);
//            Translator.translate = translates[0];
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            Log.d("jh2", "InitAysncTask doInBackground: ");
//            Translate translate = null;
//            try {
//                translate = getTranslateService();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                publishProgress(translate);
//            }
//            return null;
//        }
//
//        public Translate getTranslateService() {
//            TextView textView = textViewReference.get();
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//
//            try (InputStream is = textView.getContext().getResources().openRawResource(R.raw.credentials)) {
//
//                //Get credentials:
//                final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
//
//                //Set credentials and get translate service:
//                TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
//                return translate = translateOptions.getService();
//
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
//            return null;
//        }
//    }
}
