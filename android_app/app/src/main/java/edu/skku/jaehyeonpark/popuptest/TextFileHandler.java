package edu.skku.jaehyeonpark.popuptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileHandler {
    Activity mActivity;
    String mFileName;

    public TextFileHandler(Activity activity) {
        this.mActivity = activity;
    }

    public void callFileOpenAcivity(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        fragment.startActivityForResult(intent, 42);
    }

    public void onActivityResult(Uri uri) {
        String str = null;
        String fileName = getFileName(uri);

        try {
            str = readFile(uri);
            saveCleanFile(fileName, str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        ParcelFileDescriptor pfd = mActivity.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fd = pfd.getFileDescriptor();
        FileReader fileReader = new FileReader(fd);
        BufferedReader br = new BufferedReader(fileReader);

        String temp;
        while ((temp = br.readLine()) != null) {
            temp = temp.trim();
            if (endsWithFullStop(temp))
                temp += "\n";
            stringBuilder.append(temp + " ");
        }

        br.close();
        fileReader.close();
        pfd.close();

        String str = stringBuilder.toString();
        str = str.replaceAll("(\\v ){2,}", "\n\n");
        return str;
    }

    private String getFileName(Uri uri) {
        String fileName;
        Cursor returnCursor =
                mActivity.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    private boolean endsWithFullStop(String str) {
        final String FULLSTOP = ".!?]\"'";
        if (str.length() < 1) return true;
        return FULLSTOP.contains(str.substring(str.length() - 1));
    }

    private void saveCleanFile(String fileName, String content) throws IOException {
        File file = new File(mActivity.getFilesDir(), fileName);
        Log.d("jh park", "saveCleanFile: " + mActivity.getFilesDir());
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    public String getContentFromFile(String fileName) {
        mFileName = fileName;
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(mActivity.getFilesDir(), fileName);
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(file);
            br = new BufferedReader(fileReader);

            String temp;
            while ((temp = br.readLine()) != null) {
                stringBuilder.append(temp);
                stringBuilder.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fileReader.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }
//
//    public void showSavedFileDialog(DialogInterface.OnClickListener dialogListener) {
//        File dir = mActivity.getFilesDir();
//        final String[] fileName = dir.list();
//        if (fileName.length > 0) {
//            new AlertDialog.Builder(mActivity)
//                    .setItems(fileName, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            int offset = getPreferences(Context.MODE_PRIVATE).getInt(fileName[which], 0);
//                            String content = textFileHandler.getContentFromFile(fileName[which]);
//                            pageHandler = new PageHandler(content, mTextView);
//                            pageHandler.pageStartOffset = offset;
//                            pageHandler.refreshPage();
//                        }).show();
//        }
//    }


}
