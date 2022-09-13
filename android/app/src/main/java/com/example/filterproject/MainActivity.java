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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
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

                String f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/t.wav";

                File file2 = new File(f);
                playWav(file2);
                binding.sampleText.setText(message);
            });

    public void func(Uri uri) throws IOException {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
    }

    public void playWav(File file){
        int minBufferSize = AudioTrack.getMinBufferSize(88200, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = 16;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 88200, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        int i = 0;
        byte[] s = new byte[bufferSize];
        try {
            FileInputStream fin = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fin);
            at.play();
            while((i = dis.read(s, 0, bufferSize)) > -1){
                //for (int j = 0; j < s.length; j++) {
                    //Log.d("1", String.valueOf(s[j]));
                //    float flo = s[j];
                //    flo = 0.2f*flo;
               //    byte b2 = (byte) flo;
                    //Log.d("2", String.valueOf(b2));
               //     s[j] = b2;
               // }
                float float_arr[] = new float[s.length];
                float float_amp[] = new float[s.length];
                byte s_amp[] = new byte[s.length];
                for (int j = 0; j < s.length; j++) {
                    float_arr[j] = s[j];
                }
                float_amp = amplify(float_arr, bufferSize);
                for (int j = 0; j < s.length; j++) {
                    s_amp[j] = (byte) float_amp[j];
                }
                at.write(s_amp, 0, i);

            }
            at.stop();
            at.release();
            dis.close();
            fin.close();

        } catch (FileNotFoundException e) {
            // TODO
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
    public native float[] amplify(float[] input, int bufferSize);
}