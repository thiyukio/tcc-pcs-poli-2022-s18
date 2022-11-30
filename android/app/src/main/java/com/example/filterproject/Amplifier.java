package com.example.filterproject;

public class Amplifier {
    private int[] audiogram;

    public Amplifier(int[] _audiogram) {
        this.audiogram = _audiogram;
    }


    public float[] amplify(float[] bandA, float[] bandB, float[] bandC, float[] bandD, float[] bandE, float[] bandF) {
        float[] output = new float[bandA.length];
        for (int t = 0; t < bandA.length; t++) {
            output[t] = (audiogram[0] * bandA[t] +
                        audiogram[1] * bandB[t] +
                        audiogram[2] * bandC[t] +
                        audiogram[3] * bandD[t] +
                        audiogram[4] * bandE[t] +
                        audiogram[5] * bandF[t])/Player.NUM_BANDS;
        }
        return output;
    }
}
