package com.example.filterproject;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Player {
    Thread mThread = null;

    private final FileExtractor mFe = new FileExtractor();
    private IIRFilter filter;

    float[] floatSamples;
    double[] doubleSamples;
    float[] floatFiltered;
    double[] doubleFiltered;
    short[] shortArray;

    double[] a = { 1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000, 172.765289700000,
            -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000, -7.89982228000000,
            0.805444390000000 };
    double[] b = { 0.00590141000000000, -0.0439812000000000, 0.140436120000000, -0.239946760000000, 0.205003030000000,
            0, -0.205003030000000, 0.239946760000000, -0.140436120000000, 0.0439812000000000, -0.00590141000000000 };

    float[] x;

    AudioTrack at;

    public void initialize(Uri uri, Context context) {
        if (mThread != null) {
            boolean success = false;
            for (int attempts = 0; attempts < 100 && !success; attempts++) {
                if (mFe.mState == FileExtractor.StateEnum.UNINITIALIZED
                        && !mThread.isAlive()) {
                    success = true;
                } else {
                    mFe.stop();
                    try {
                        mThread.join(1000);
                    } catch (InterruptedException e) {
                        Log.e("Player", "Failed");
                    }
                }
            }
            if (!success) {
                Log.e("Player", "Failed to stop Player, try again later");
                return;
            }
            mThread = null;
        }
        boolean success = false;
        for (int attempts = 0; attempts < 1000 && !success; attempts++) {
            try {
                if (mFe.initialize(context.getApplicationContext(), uri) != null) {
                    success = true;
                }
            } catch (IOException e) {
                Log.e("Player", "Failed to find file, trying again");
            }
        }
        if (!success) {
            Log.e("Player", "Failed to find file, try again later");
            return;
        }

        int sampleRate = mFe.getSampleRate();
        int numChannels = mFe.getNumChannels();

        if (numChannels > 2 || numChannels < 1) {
            Log.e("Player", "Only supports mono or stereo");
            return;
        }

        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT);
        at = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT, minBufferSize,
                AudioTrack.MODE_STREAM);

        at.play();
        x = new float[20];

        floatSamples = new float[0];
        doubleSamples = new double[0];
        floatFiltered = new float[0];
        doubleFiltered = new double[0];
        shortArray = new short[0];

        filter = new IIRFilter(a, b);
    }

    public void start() {
        if (mFe.start() == null) {
            Log.e("Player", "Failed to start fileExtractor, try again later");
            return;
        }
        Player thisPlayer = this;
        mThread = new Thread(thisPlayer::run);
        mThread.start();
    }

    private void run() {
        for (boolean iterate = true; iterate;) {
            ByteBuffer bbuf = mFe.dequeueOutputBuffer();
            if (bbuf == null) {
                if (mFe.finishedExtracting) {
                    iterate = false;
                }
                continue;
            }

            int numChannels = mFe.getNumChannels();
            int isStereo = numChannels == 2 ? 1 : 0;
            int bufferSizeInBytes = bbuf.limit() - bbuf.position();
            int desiredFloatArraySize = bufferSizeInBytes / 2;
            if (desiredFloatArraySize > floatSamples.length) {
                shortArray = new short[desiredFloatArraySize];
                doubleSamples = new double[desiredFloatArraySize / numChannels];
                floatFiltered = new float[desiredFloatArraySize / numChannels];
                doubleFiltered = new double[desiredFloatArraySize / numChannels];
            }

            bbuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray, 0, desiredFloatArraySize);

            for (int t = 0; t < desiredFloatArraySize / numChannels; t++) {
                doubleSamples[t] = (double) (shortArray[numChannels * t] + shortArray[numChannels * t + isStereo]) / 0x10000;
                }

            filter.process(doubleSamples, floatFiltered, desiredFloatArraySize / numChannels);

            at.write(floatFiltered, 0, desiredFloatArraySize / numChannels, AudioTrack.WRITE_BLOCKING);
            mFe.releaseOutputBuffer();

        }
        filter = null;
        at.stop();
        at.release();
    }
}
