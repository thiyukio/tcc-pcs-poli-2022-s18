package com.example.filterproject;
import static androidx.core.content.ContextCompat.getSystemService;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class Amplifier {
    private int audiogram;
    private float g, Pm, d, pl, pd, k, f, x2_min, cmax, lambda, delta, Pd, R, x2_m;
    public Amplifier(float sensibilidade, float f, float R, float lambda, float Pl, int A) {
        g = (float) pow(10, (sensibilidade/10)); this.f = f; this.R = R; this.lambda = lambda;
        Pm = 10*((float) log10(g*pow(f, 2)*(1/R)*pow(10, 3)));
        Pd = Pl + A; //Limiar para pessoa com deficiencia
        delta = (Pm - Pd)/(Pm - Pl);
        d = (delta-1)/2;
        pl = (float) pow(10,(Pl/10));
        pd = (float) pow(10,(Pd/10));
        k = (float) (sqrt(pd/pow(pl,delta))*pow(g*pow(f,2)*pow(10,3)/R,d));
        x2_min = (float) (R*pl/(g*pow(f,2)*pow(10,3)));
        cmax = (float) sqrt(pd/pl);
    }


    public void amplify(float[] amostras, int N,  float volume) {
        int i; float c;
        for(i = 0; i < N; i++){
            x2_m = (float) (lambda * x2_m + (1 - lambda) * pow( amostras[i], 2 ));
            if(volume*volume*x2_m < x2_min)
            {
                c = cmax;
            }
            else
            {
                c = (float) (k * pow(volume*volume*x2_m, d));
            }
            amostras[i] = c*amostras[i];
        }
    }
}
