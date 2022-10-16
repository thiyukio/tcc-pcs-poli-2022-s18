package com.example.filterproject;

import android.util.Log;

import java.util.Arrays;

public class IIRFilter {

    public IIRFilter(double a_[], double b_[]) {
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
    public void process(double input[], float output[], int size) {
        for (int i = 0; i < size; i++) {

            x[m] = input[i];
            yaux = b[0]*x[m];
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
            output[i] = (float )yaux;

            m++;
            if (m >= N)  //m = (m mod N);
            {
                //Log.i("audioFloats1", Arrays.toString(x));
                //Log.i("audioFloats2", Arrays.toString(y));
                m = 0;
            }
            /*
            float in  = input[i];
            float out = 0.0f;
            for (int j = memory.length-1; j >= 0; j--) {
                in  -= a[j+1] * memory[j];
                out += b[j+1] * memory[j];
            }
            out += b[0] * in;
            output[i] = out;
            // shift memory
            for (int j = memory.length-1; j > 0; j--) {
                memory[j] = memory[j - 1];
            }
            memory[0] = in;
             */

        }
        //Log.i("MyAndroidClass", Arrays.toString(output));

    }
    public void process2(){
        Log.d("valor22",String.valueOf(m));
        m++;
    }

    private double[] a;
    private double[] b;
    private int m;
    int N;
    private double yaux;
    private double[] x;
    private double[] y;
    int k;
}