package com.example.filterproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Player {

    public static int NUM_BANDS = 6;

    private MediaExtractor mExtractor;

    float[] floatSamples;
    double[] doubleSamples;
    float[] floatFiltered;
    double[] doubleFiltered;
    short[] shortArray;

    private IIRFilter[] filters = new IIRFilter[NUM_BANDS];

    AudioTrack mAt;
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

        FileDescriptor mFd = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();

        mExtractor = createMediaExtractor(mFd);

        MediaFormat mFormat = mExtractor.getTrackFormat(0);

        mCodec = createMediaCodec(mFormat);
        mCodec.configure(mFormat, null, null, 0);

        Integer sampleRate = mFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        Integer numChannels = mFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        filters = FilterConstants.getFilters(sampleRate);

        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_FLOAT);
        mAt = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(minBufferSize * 3)
                .build();

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

                int isStereo = numChannels == 2 ? 1 : 0;
                int bufferSizeInBytes = info.size;
                int desiredDoubleArraySize = bufferSizeInBytes / 2;
                if (desiredDoubleArraySize > shortArray.length) {
                    doubleSamples = new double[desiredDoubleArraySize / numChannels];
                    shortArray = new short[desiredDoubleArraySize];
                    floatFiltered = new float[desiredDoubleArraySize / numChannels];
                }

                outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray, 0,
                        desiredDoubleArraySize);

                for (int t = 0; t < desiredDoubleArraySize / numChannels; t++) {
                    // Importante ser blocking
                    doubleSamples[t] = (double) (shortArray[numChannels * t] + shortArray[numChannels * t + isStereo])
                            / 0x10000;
                }

                filters[1].process(doubleSamples, floatFiltered, desiredDoubleArraySize / numChannels);

                mAt.write(floatFiltered, 0, desiredDoubleArraySize / numChannels, AudioTrack.WRITE_BLOCKING);

                mCodec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        });

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
