package edu.skku.jaehyeonpark.popuptest;

import java.util.ArrayList;

public class MWEResult {
    private String MWE;
    private String meaning;
    private ArrayList<int []> offsets;

    public String getMWE() {
        return MWE;
    }

    public void setMWE(String MWE) {
        this.MWE = MWE;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public ArrayList<int[]> getOffsets() {
        return offsets;
    }

    public void setOffsets(ArrayList<int[]> offsets) {
        this.offsets = offsets;
    }
}
