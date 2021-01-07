package edu.skku.jaehyeonpark.popuptest;

import android.text.Layout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.text.BreakIterator;

public class PageHandler {
    private static final String TAG = "jh park";
    String content;
    TextView mTextView;
    int pageStartOffset = 0;
    final int PAGE_READ_SIZE = 3000;
    Stack<Integer> prevPageStack = new Stack<>();

    public void setTextView(TextView textView) {
        this.mTextView = textView;
    }

    public PageHandler(String content, TextView mTextView) {
        this.content = content;
        this.mTextView = mTextView;
        Log.d(TAG, "PageHandler: " +content.length());
    }

    public void nextPage() {
        //last page
        if (content.length() <= pageStartOffset + mTextView.getText().length()) {
            return;
        }

        prevPageStack.push(pageStartOffset);
        pageStartOffset += mTextView.getText().length();
        Log.d(TAG, "nextPage: " + pageStartOffset);
        refreshPage();
        Log.d(TAG, "nextPage: " + pageStartOffset);
    }

    public void prevPage() {
        if (pageStartOffset <= 0) {
            return;
        }

        if (!prevPageStack.isEmpty()) {
            int prevOffset = prevPageStack.pop();
            mTextView.setText(content.substring(prevOffset, pageStartOffset));
            pageStartOffset = prevOffset;
            return;
        }


        if (pageStartOffset - PAGE_READ_SIZE <= 0) {
            mTextView.setText(content.substring(0, pageStartOffset));
        } else {
            mTextView.setText(content.substring(pageStartOffset - PAGE_READ_SIZE, pageStartOffset));
        }

        Layout layout = mTextView.getLayout();
        int i;
        if ((i = getFullLineCount()) == -1) {
            pageStartOffset = 0;
            return;
        }

        int tempStartOffset = pageStartOffset;
        pageStartOffset = layout.getOffsetForHorizontal(layout.getLineCount() - i, 0);
        mTextView.setText(content.substring(pageStartOffset, tempStartOffset));
        return;
    }


    private void trim() {
        Layout layout = mTextView.getLayout();
        String text = mTextView.getText().toString();

        int i;
        if ((i = getFullLineCount()) == -1) return;

        int nextPageFirstOffset = layout.getOffsetForHorizontal(i, 0);

        BreakIterator boundary = BreakIterator.getSentenceInstance();
        boundary.setText(text);
        nextPageFirstOffset = boundary.preceding(nextPageFirstOffset) > 0 ?
                boundary.preceding(nextPageFirstOffset):
                nextPageFirstOffset;

        mTextView.setText(mTextView.getText().toString().substring(0, nextPageFirstOffset));
    }

    private int getFullLineCount() {
        Layout layout = mTextView.getLayout();

        if (layout.getLineBottom(layout.getLineCount() - 1) < mTextView.getHeight()) {
            return -1;
        }

        int i = 0;
        while (layout.getLineBottom(i) < mTextView.getHeight()) {
            i++;
        }

        return i;
    }

    public void refreshPage() {
        if (content.length() < pageStartOffset + PAGE_READ_SIZE) {
            mTextView.setText(content.substring(pageStartOffset));
        } else {
            mTextView.setText(content.substring(pageStartOffset, pageStartOffset + PAGE_READ_SIZE));
        }

        trim();
    }


    public int toGlobalOffset(int offset){
        return pageStartOffset + offset;
    }

    private String findSentence(int offset) {
        int startOffset, endOffset;
        startOffset = endOffset = toGlobalOffset(offset);

        while (startOffset >= 0 && !isFullStop(content.charAt(startOffset))) {
            startOffset--;
        }

        while (endOffset < content.length() && !isFullStop(content.charAt(startOffset))) {
            endOffset++;
        }

        return content.substring(startOffset==0?startOffset:startOffset+1, endOffset).trim();
    }

    private boolean isFullStop(char ch){
        final String FULLSTOP = ".!?:;\n}]>";
        return FULLSTOP.contains(""+ch);
    }

    private String findParagraph(int offset) {
        int startOffset, endOffset;
        startOffset = endOffset = toGlobalOffset(offset);

        while (startOffset >= 0 && !isFullStop(content.charAt(startOffset))) {
            startOffset--;
        }
        if(startOffset > 5){
            startOffset-=2;
            while (startOffset >= 0 && !isFullStop(content.charAt(startOffset))) {
                startOffset--;
            }
        }

        while (endOffset < content.length() && !isFullStop(content.charAt(startOffset))) {
            endOffset++;
        }
        if(endOffset < content.length() - 5){
            endOffset+=2;
            while (endOffset < content.length() && !isFullStop(content.charAt(startOffset))) {
                endOffset++;
            }
        }

        return content.substring(startOffset==0?startOffset:startOffset+1, endOffset).trim();
    }

}
