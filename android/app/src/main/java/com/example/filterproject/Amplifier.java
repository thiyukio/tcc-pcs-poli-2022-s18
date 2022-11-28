package com.example.filterproject;

public class Amplifier {


    public float[] amplify(float[] bandA, float[] bandB, float[] bandC, float[] bandD, float[] bandE, float[] bandF) {
        float[] output = new float[bandA.length];
        for (int t = 0; t < bandA.length; t++) {
            output[t] = (bandA[t] + bandB[t] + bandC[t] + bandD[t] + bandE[t] + bandF [t])/Player.NUM_BANDS;
        }
        return output;
    }
}
