package com.example.filterproject;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Player {
    Thread mThread = null;

    private FileExtractor mFe = new FileExtractor();
    private Uri mUri;
    private IIRFilter filter;

    float[] floatSamples;
    double[] doubleSamples;
    float[] floatFiltered;
    double[] doubleFiltered;
    short[] shortArray;

    //float h[]= {-0.0978f, 0.0877f, 0.0729f, 0.0698f, 0.0726f, 0.0780f, 0.0843f, 0.0897f, 0.0938f, 0.0961f, 0.0961f, 0.0938f, 0.0897f, 0.0843f, 0.0780f, 0.0726f, 0.0698f, 0.0729f, 0.0877f, -0.0978f};
    float h[]={-0.0930250949393319f, 0.0207057333964976f, 0.0291475759590533f, 0.0430501166124715f, 0.0601692514455458f, 0.0788068471187608f, 0.0968422736437664f, 0.112413676993475f, 0.123818015340460f, 0.129851026602985f, 0.129851026602985f, 0.123818015340460f, 0.112413676993475f, 0.0968422736437664f, 0.0788068471187608f, 0.0601692514455458f, 0.0430501166124715f, 0.0291475759590533f, 0.0207057333964976f, -0.0930250949393319f};
    double[] a = {1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000, 172.765289700000, -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000, -7.89982228000000, 0.805444390000000};
    double[] b = {0.00590141000000000, -0.0439812000000000, 0.140436120000000, -0.239946760000000, 0.205003030000000, 0, -0.205003030000000, 0.239946760000000, -0.140436120000000, 0.0439812000000000, -0.00590141000000000};

    float[] x;

    AudioTrack at;

    public void initialize (Uri uri, Context context) {
        if (mThread != null) {
            boolean success = false;
            for (int attempts = 0; attempts < 100 && success == false; attempts++) {
                if (mFe.mState == FileExtractor.StateEnum.UNINITIALIZED
                        && mThread.isAlive() == false) {
                    success = true;
                } else {
                    mFe.stop();
                    try {
                        mThread.join(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (success == false) {
                Log.e("Player", "Failed to stop Player, try again later");
                return;
            }
            mThread = null;
        }
        boolean success = false;
        for (int attempts = 0; attempts < 1000 && success == false; attempts++) {
            try {
                if (mFe.initialize(context.getApplicationContext(), uri) != null) {
                    success = true;
                }
            } catch (IOException e) {
                Log.e("Player", "Failed to find file, trying again");
            }
        }
        if (success == false) {
            Log.e("Player", "Failed to find file, try again later");
            return;
        }

        int sampleRate = mFe.getSampleRate();
        int numChannels = mFe.getNumChannels();

        if (numChannels > 2 || numChannels < 1) {
            Log.e("Player", "Only supports mono or stereo");
            return;
        }

        int channelConfig = numChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;

        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_FLOAT);
        int bufferSize = 512;
        at = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, channelConfig,
                AudioFormat.ENCODING_PCM_FLOAT, minBufferSize,
                AudioTrack.MODE_STREAM);


        byte[] s = new byte[bufferSize];
        at.play();
        x = new float[20];
        byte[] s_amp = new byte[s.length];
        //float aux;

        floatSamples = new float[0];
        doubleSamples = new double[0];
        floatFiltered = new float[0];
        doubleFiltered = new double[0];
        shortArray = new short[0];

        filter = new IIRFilter(a, b);
    }

    public void start () {
        if (mFe.start() == null) {
            Log.e("Player", "Failed to start fileExtractor, try again later");
            return;
        }
        Player thisPlayer = this;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                thisPlayer.run ();
            }
        });
        mThread.start();
    }

    private void run () {
        for (boolean iterate = true; iterate == true;) {
            ByteBuffer bbuf = mFe.dequeueOutputBuffer();
            if (bbuf == null) {
                if (mFe.finishedExtracting) {
                    iterate = false;
                }
                continue;
            }

            int bufferSizeInBytes = bbuf.limit() - bbuf.position();
            int desiredFloatArraySize = bufferSizeInBytes / 2;
            if (desiredFloatArraySize > floatSamples.length) {
//                floatSamples = new float[desiredFloatArraySize];
                shortArray = new short[desiredFloatArraySize];
                doubleSamples = new double[desiredFloatArraySize];
                floatFiltered = new float[desiredFloatArraySize];
                doubleFiltered = new double[desiredFloatArraySize];
            }
//            for (int i = 0; bbuf.limit() - bbuf.position() > 0; i++) {
//                Short sample = bbuf.getShort();
//                Float sampleFloat = (float) sample / 0x8000;
            bbuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray, 0, desiredFloatArraySize);

            for (int t = 0; t < desiredFloatArraySize; t++) {
                doubleSamples[t] = ((double) shortArray[t]) / 0x8000;
            }


            filter.process(doubleSamples, floatFiltered, desiredFloatArraySize);

            // NÃO usar floatSamples.length!!!!!
            // bufferSizeInBytes/2 pode ser menor que floatSamples.length
//            for (int l = 0; l < bufferSizeInBytes / 2; l++) {
//                float aux = 0;
//                for (int j = 0; j < 19; j++) {
//                    x[j] = x[j + 1];
//                }
//                x[19] = floatSamples[l];
//                aux = 0.0f;
//                for (int k = 0; k < 20; k++) {
//                    aux += h[k] * x[19 - k];
//                }
//                floatFiltered[l] = aux;
//            }

            at.write(floatFiltered, 0, bufferSizeInBytes / 2, AudioTrack.WRITE_BLOCKING);
            mFe.releaseOutputBuffer();

//            ShortBuffer sbuf = bbuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//
//            short[] audioShorts = new short[sbuf.capacity()];
//            sbuf.get(audioShorts);
//
//            float[] audioFloats = new float[audioShorts.length];
//            for (int t = 0; t < audioShorts.length; t++) {
//                audioFloats[t] = ((float)audioShorts[t])/0x8000;
//            }
//
//            //filter.process(audioFloats,float_amp);
//
//            Log.i("MyAndroidClass1", String.valueOf(audioFloats.length));
//            Log.i("MyAndroidClass2", Byte.toString(s_amp[10]));
////
//
//            at.write(audioFloats, 0, 256, AudioTrack.WRITE_BLOCKING);
        }
        filter = null;
        at.stop();
        at.release();
    }
}
