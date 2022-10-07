package com.example.filterproject;

import static android.media.AudioFormat.*;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileExtractor {
    private MediaExtractor mExtractor;
    private MediaFormat mFormat;
    private String mMime;
    private FileDescriptor mFd;
    private MediaCodec mCodec;
    public MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    public boolean isExtracting = true;
    public boolean isDecoding = true;

    private int outputBufferIdx;

    static private MediaCodec createMediaCodec (MediaFormat format)
            throws IOException, IllegalArgumentException {
        assert format != null;
        String codecName = new MediaCodecList(MediaCodecList.ALL_CODECS).findDecoderForFormat(format);
        MediaCodec codec = null;
        String mime = format.getString(MediaFormat.KEY_MIME);
        //Log.d(, "try to create a codec mime="+mime+" codecName="+codecName);
        if (codecName != null)
            codec = MediaCodec.createByCodecName(codecName);
        else if (mime != null)
            codec = MediaCodec.createDecoderByType(mime);  // may be throw IllegalArgumentException
        return codec;
    }

    static private MediaExtractor createMediaExtractor (FileDescriptor fd) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(fd);
        extractor.selectTrack(0);

        return extractor;
    }

    public void initialize (Context context, Uri uri)
            throws IOException {
        mFd = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();

        mExtractor = createMediaExtractor(mFd);

        mFormat = mExtractor.getTrackFormat(0);

        mMime = mFormat.getString(MediaFormat.KEY_MIME);

        mCodec = createMediaCodec(mFormat);
        mCodec.configure(mFormat, null, null, 0);
    }

    public void start () {
        mCodec.start ();

        FileExtractor thisExtractor = this;
        new Thread (new Runnable () {
            @Override
            public void run () {
                thisExtractor.run ();
            }
        }).start ();
    }

    private void run () {
        while (isExtracting) {
            int inputBufferId = mCodec.dequeueInputBuffer(0);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferId);
                int size = mExtractor.readSampleData(inputBuffer, 0);
                long time = mExtractor.getSampleTime();
                if (size >= 0) {
                    mCodec.queueInputBuffer(
                            inputBufferId, 0, size,
                            time, 0
                    );

                    mExtractor.advance();
                } else {
                    isExtracting = false;
                    mCodec.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    );
                }
            } else {
                Log.v("FileExtrator", "No codecInputBuffers left");
            }
        }
    }

    public ByteBuffer dequeueOutputBuffer () {
        outputBufferIdx =  mCodec.dequeueOutputBuffer(info, 0);
        if (outputBufferIdx >= 0) {
            return mCodec.getOutputBuffer(outputBufferIdx);
        } else {
            Log.v("FileExtrator", "No codecOutputBuffers left");
            return null;
        }
    }

    public void releaseOutputBuffer () {
        mCodec.releaseOutputBuffer(outputBufferIdx, 0);
    }

    private static ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public Integer getSampleRate () {
        return mFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    }

    public Integer getNumChannels () {
        return mFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
    }

    public void stop (Context context) {
        mCodec.release();
        mCodec.stop();
    }
}
