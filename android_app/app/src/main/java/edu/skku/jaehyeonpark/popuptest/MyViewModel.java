package edu.skku.jaehyeonpark.popuptest;

import android.app.Activity;

import androidx.lifecycle.ViewModel;

class MyViewModel extends ViewModel {
    String content = null;

    public String getContent(Activity activity, String fileName){
        if(content == null){
            TextFileHandler textFileHandler = new TextFileHandler(activity);
            content = textFileHandler.getContentFromFile(fileName);
        }

        return content;
    }
}
