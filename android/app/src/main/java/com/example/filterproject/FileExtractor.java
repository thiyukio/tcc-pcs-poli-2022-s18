package com.example.filterproject;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileExtractor {
    public enum StateEnum {
        UNINITIALIZED,
        INITIALIZED,
        EXECUTING,
    }
    volatile public StateEnum mState = StateEnum.UNINITIALIZED;

    volatile private MediaExtractor mExtractor;
    private MediaFormat mFormat;
    private String mMime;
    private FileDescriptor mFd;
    volatile private MediaCodec mCodec;
    public MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    volatile public boolean requestStop = false;

    volatile public boolean isExtracting = false;
    volatile public boolean finishedExtracting = false;
    volatile public boolean isDecoding = false;
    public boolean decoderEndOfStream = false;

    private Thread mThread;

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

    public FileExtractor initialize (Context context, Uri uri)
            throws IOException {
        if (mState != StateEnum.UNINITIALIZED) {
            return null;
        }
        mFd = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();

        mExtractor = createMediaExtractor(mFd);

        mFormat = mExtractor.getTrackFormat(0);

        mMime = mFormat.getString(MediaFormat.KEY_MIME);

        mCodec = createMediaCodec(mFormat);
        mCodec.configure(mFormat, null, null, 0);

        isExtracting = false;
        isDecoding = false;
        requestStop = false;
        finishedExtracting = false;
        decoderEndOfStream = false;

        mState = StateEnum.INITIALIZED;

        return this;
    }

    public FileExtractor start () {
        if (mState != StateEnum.INITIALIZED) {
            return null;
        }

        mCodec.start ();

        FileExtractor thisExtractor = this;
        mThread = new Thread (new Runnable () {
            @Override
            public void run () {
                thisExtractor.run ();
            }
        });
        mThread.start ();

        mState = StateEnum.EXECUTING;

        return this;
    }

    private void run () {
        while (finishedExtracting == false && requestStop == false) {
            // Log.v("", "comeco while");
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
                    finishedExtracting = true;
                    mCodec.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    );
                }
            } else {
                // Log.v("Extrator", "No codecInputBuffers left");
            }
        }
        finishedExtracting = true;
        onFinishedExtracting();
    }

    public ByteBuffer dequeueOutputBuffer () {
        if (requestStop == false) {
            isDecoding = true;
            outputBufferIdx =  mCodec.dequeueOutputBuffer(info, 0);
            if (outputBufferIdx >= 0) {
                return mCodec.getOutputBuffer(outputBufferIdx);
            } else {
                isDecoding = false;
                Log.v("FileExtrator", "No codecOutputBuffers left");

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    decoderEndOfStream = true;
                }
            }
        } else {
            decoderEndOfStream = true;
        }
        return null;
    }

    public void releaseOutputBuffer () {
        if (requestStop == false) {
            mCodec.releaseOutputBuffer(outputBufferIdx, false);
        } else {
            decoderEndOfStream = true;
        }
        isDecoding = false;
        onFinishedDecodingBuffer();
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

    public void stop () {
        requestStop = true;
        try {
            mThread.join();
        } catch (InterruptedException e) {
        }
        if (decoderEndOfStream && finishedExtracting) {
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
                mCodec = null;
            }
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
            if (mCodec == null && mExtractor == null) {
                mState = StateEnum.UNINITIALIZED;
            }
        }
    }

    private void onFinishedExtracting () {
        if (requestStop) {
            //stop();
//            if (isDecoding == false && mState == StateEnum.EXECUTING) {
//                Log.v("erw", "Stopped onFinishedExtracting");
//                decoderEndOfStream = true;
//                if (mCodec != null) {
//                    if (mState == StateEnum.EXECUTING) {
//                        mCodec.stop();
//                        mCodec.release();
//                    }
//                    mCodec = null;
//                }
//                if (mExtractor != null) {
//                    mExtractor.release();
//                    mExtractor = null;
//                }
//                if (mCodec == null && mExtractor == null) {
//                    mState = StateEnum.UNINITIALIZED;
//                }
//            }
        }
    }
    private void onFinishedDecodingBuffer () {
        if (requestStop) {
            //stop();
//            if (finishedExtracting == true && mState == StateEnum.EXECUTING) {
//                Log.v("erw", "Stopped onFinishedDecodingBuffer");
//                decoderEndOfStream = true;
//                if (mCodec != null) {
//                    if (mState == StateEnum.EXECUTING) {
//                        mCodec.stop();
//                        mCodec.release();
//                    }
//                    mCodec = null;
//                }
//                if (mExtractor != null) {
//                    mExtractor.release();
//                    mExtractor = null;
//                }
//                if (mCodec == null && mExtractor == null) {
//                    mState = StateEnum.UNINITIALIZED;
//                }
//            }
        }
    }
}
