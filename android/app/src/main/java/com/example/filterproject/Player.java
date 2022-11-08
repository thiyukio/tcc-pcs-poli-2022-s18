package com.example.filterproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

public class Player {

    private Uri mUri;
    private FileDescriptor mFd;
    private MediaExtractor mExtractor;

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
    private IIRFilter filter = new IIRFilter(a, b);

    AudioTrack mAt;
    private MediaFormat mFormat;
    private String mMime;
    private MediaCodec mCodec;

    static private MediaCodec createMediaCodec(MediaFormat format)
            throws IOException, IllegalArgumentException {
        assert format != null;
        String codecName = new MediaCodecList(MediaCodecList.ALL_CODECS).findDecoderForFormat(format);
        MediaCodec codec = null;
        String mime = format.getString(MediaFormat.KEY_MIME);
        // Log.d(, "try to create a codec mime="+mime+" codecName="+codecName);
        if (codecName != null)
            codec = MediaCodec.createByCodecName(codecName);
        else if (mime != null)
            codec = MediaCodec.createDecoderByType(mime); // may be throw IllegalArgumentException
        return codec;
    }

    static private MediaExtractor createMediaExtractor(FileDescriptor fd) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(fd);
        extractor.selectTrack(0);

        return extractor;
    }

    @SuppressLint("NewApi")
    public void initialize(Uri uri, Context context) throws IOException {
        stop();

        mUri = uri;
        mFd = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();

        mExtractor = createMediaExtractor(mFd);

        mFormat = mExtractor.getTrackFormat(0);

        mMime = mFormat.getString(MediaFormat.KEY_MIME);

        mCodec = createMediaCodec(mFormat);
        mCodec.configure(mFormat, null, null, 0);

        Integer sampleRate = mFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        Integer numChannels = mFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        // int channelConfig = numChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO :
        // AudioFormat.CHANNEL_OUT_STEREO;
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;

        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_FLOAT);
        int bufferSize = 512;
        mAt = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(minBufferSize)
                .build();

        // Log.v("gdf", "Sample Rate = "+sampleRate+", numChannels = "+numChannels);

        floatSamples = new float[0];
        doubleSamples = new double[0];
        floatFiltered = new float[0];
        doubleFiltered = new double[0];
        shortArray = new short[0];

        mCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                if (inputBuffer == null) {
                    return;
                }
                int size = mExtractor.readSampleData(inputBuffer, 0);
                if (size < 0) {
                    return;
                }
                long time = size > 0 ? mExtractor.getSampleTime() : 0;
                int flags = size > 0 ? 0 : MediaCodec.BUFFER_FLAG_END_OF_STREAM;

                codec.queueInputBuffer(index, 0, size, time, flags);
                mExtractor.advance();
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                    @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = mCodec.getOutputBuffer(index);
                if (outputBuffer == null) {
                    return;
                }

                int bufferSizeInBytes = info.size;
                int desiredDoubleArraySize = bufferSizeInBytes / 4;
                if (desiredDoubleArraySize * 2 > shortArray.length) {
                    doubleSamples = new double[desiredDoubleArraySize];
                    shortArray = null;
                    shortArray = new short[desiredDoubleArraySize * 2];
                    floatFiltered = new float[desiredDoubleArraySize];
                }

                outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray, 0,
                        desiredDoubleArraySize * 2);

                for (int t = 0; t < desiredDoubleArraySize; t++) {
                    // Importante ser blocking
                    doubleSamples[t] = (((double) shortArray[2 * t]) / 0x8000);
                }

                // Simulação de vários filtros
                for (int i = 0; i < 50; i++) {
                    filter.process(doubleSamples, floatFiltered, desiredDoubleArraySize);
                }

                mAt.write(floatFiltered, 0, desiredDoubleArraySize, AudioTrack.WRITE_BLOCKING);

                mCodec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        });

        return;
    }

    public void start() {
        mAt.play();
        mCodec.start();
    }

    public void stop() {
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
        if (mAt != null) {
            mAt.release();
        }
    }
}
