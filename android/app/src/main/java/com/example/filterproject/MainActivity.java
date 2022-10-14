package com.example.filterproject;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filterproject' library on application startup.
    static {
        System.loadLibrary("filterproject");
    }


    private ActivityMainBinding binding;

    public File file;

    FloatingActionButton pickAFileButton;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(

            new ActivityResultContracts.GetContent(),
            uri -> {
                String message = String.format(
                        "Consegui a uri = %s",
                        uri
                );

                Log.d("myTag", message);
                this.file = new File(uri.getPath());

                Log.d("tag2", uri.getPath());

                String f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/LP2.wav";

                File file2 = new File(f);
                Log.d("nome", file2.getPath());
                try {
                    playWav(file2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                binding.sampleText.setText(message);
            });

    public void func(Uri uri) throws IOException {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
    }

    public void playWav(File file) throws IOException {
        int minBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        int bufferSize = 32768;
        int bufferSize2 = bufferSize / 2;
        int bufferSize4 = bufferSize / 4;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT, minBufferSize, AudioTrack.MODE_STREAM);

        MediaExtractor me = new MediaExtractor();
        me.setDataSource(file.getPath());
        me.selectTrack(0);


        int i;

        byte[] s = new byte[bufferSize];
        try {
            FileInputStream fin = new FileInputStream(file);
            Log.d("nome2", file.getName());
            DataInputStream dis = new DataInputStream(fin);
            at.play();
            double[] audioFloats = new double[bufferSize4];
            float[] audioFloats2 = new float[bufferSize4];
            float float_amp[] = new float[bufferSize4];
            for (int h = 0; h < float_amp.length; h++) {
                float_amp[h] = 0.0f;
            }
            //float x[] = new float[20];
            //float h[]= {-0.0978f, 0.0877f, 0.0729f, 0.0698f, 0.0726f, 0.0780f, 0.0843f, 0.0897f, 0.0938f, 0.0961f, 0.0961f, 0.0938f, 0.0897f, 0.0843f, 0.0780f, 0.0726f, 0.0698f, 0.0729f, 0.0877f, -0.0978f};
            double[] a = {1.000000000000, -9.39331398000000, 40.0758751800000, -102.252093950000, 172.765289700000, -201.969268100000, 165.442085090000, -93.7680344200000, 35.1938395300000, -7.89982228000000, 0.805444390000000};
            double[] b = {0.00590141000000000, -0.0439812000000000, 0.140436120000000, -0.239946760000000, 0.205003030000000, 0, -0.205003030000000, 0.239946760000000, -0.140436120000000, 0.0439812000000000, -0.00590141000000000};
            IIRFilter filter = new IIRFilter(a, b);
            /*
            while ((i = dis.read(s, 0, bufferSize)) > -1) {

                ShortBuffer sbuf =
                        ByteBuffer.wrap(s).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                short[] audioShorts = new short[sbuf.capacity()];
                sbuf.get(audioShorts);

                for (int t = 0; t < bufferSize4; t++) {
                    audioFloats[t] = ((double) audioShorts[2 * t]) / 0x8000;
                }

                filter.process(audioFloats, float_amp);

                Log.i("Float_Amp", Arrays.toString(audioFloats));
                //Log.v("float_amp",String.valueOf(float_amp[5]));
                //Log.i("audioFloats", Arrays.toString(audioFloats));
                //Log.i("Float_Amp", Arrays.toString(float_amp));
                at.write(float_amp, 0, bufferSize4, AudioTrack.WRITE_BLOCKING);
            }
             */
            float[] float_amp2 = new float[bufferSize4];
            ByteBuffer bf = ByteBuffer.allocate(32768);
            while ((i = (me.readSampleData(bf,0))) > -1){
                //Log.d("tam amostra", String.valueOf(i));
                ShortBuffer sbuf =
                        bf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                short[] audioShorts = new short[sbuf.capacity()];
                sbuf.get(audioShorts);

                for (int t = 0; t < bufferSize4; t++) {
                    audioFloats[t] = ((double) audioShorts[2 * t]) / 0x8000;
                }

                filter.process(audioFloats, float_amp);

                //for (int t = 0; t < bufferSize4; t++) {
                //    audioFloats2[t] = ((float) audioFloats[t]);
                //}
                //Log.i("Float_Amp", Arrays.toString(audioFloats));
                //Log.v("float_amp",String.valueOf(float_amp[5]));
                //Log.i("audioFloats", Arrays.toString(audioFloats));
                //Log.i("Float_Amp", Arrays.toString(float_amp));
                at.write(float_amp, 0, bufferSize4, AudioTrack.WRITE_BLOCKING);
                me.advance();
            }


            at.stop();
            at.release();
            dis.close();
            fin.close();

        } catch (FileNotFoundException e) {
            // TODO
            Log.d("erro", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pickAFileButton = binding.pickAFileButton;

        pickAFileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start file chooser
                mGetContent.launch("audio/*");
            }
        });

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        tv.setText(Float.toString(test()[0]) + ", " + Float.toString(test()[1]));
        // tv.setText(test().toString());
    }


    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */
    public native float[] test();

}