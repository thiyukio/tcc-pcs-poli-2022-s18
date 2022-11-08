package com.example.filterproject;

import android.util.Log;

import java.util.Arrays;

public class IIRFilter {

    public IIRFilter(double[] a_, double[] b_) {
        // initialize memory elements
        N = Math.max(a_.length, b_.length);
        x = new double[N]; y = new double[N]; m = 0;
        for (int i = 0; i < x.length; i++) {
            x[i] = 0.0f;
            y[i] = 0.0f;
        }
        // copy filter coefficients
        a = new double[N];
        int i = 0;
        for (; i < a_.length; i++) {
            a[i] = a_[i];
        }
        for (; i < N; i++) {
            a[i] = 0.0f;
        }
        b = new double[N];
        i = 0;
        for (; i < b_.length; i++) {
            b[i] = b_[i];
        }
        for (; i < N; i++) {
            b[i] = 0.0f;
        }
    }

    // Filter samples from input buffer, and store result in output buffer.
    // Implementation based on Direct Form II.
    // Works similar to matlab's "output = filter(b,a,input)" command
    public void process(double[] input, float[] output, int size) {
        for (int i = 0; i < size; i++) {


            x[m] = input[i];
            double yaux = b[0] * x[m];
            k = 1;
            while(k <= m)
            {
                yaux += b[k]*x[m-k] - a[k]*y[m-k];
                k++;
            }
            while(k < N)
            {
                yaux += b[k]*x[m+N-k] - a[k]*y[m+N-k];
                k++;
            }
            y[m] = yaux;
            output[i] = (float ) yaux;

            m++;
            if (m >= N)  //m = (m mod N);
            {
                m = 0;
            }

        }

    }

    private double[] a;
    private double[] b;
    private int m;
    int N;
    private double[] x;
    private double[] y;
    int k;
}