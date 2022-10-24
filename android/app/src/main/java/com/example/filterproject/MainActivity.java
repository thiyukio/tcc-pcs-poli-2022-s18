package com.example.filterproject;

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
import android.util.Log;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filterproject' library on application startup.
    static {
        System.loadLibrary("filterproject");
    }


    private ActivityMainBinding binding;

    public File file;
    Player mPlayer = new Player();

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

                playWav(uri);

                binding.sampleText.setText(message);
            });

    public void func(Uri uri) throws IOException {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
    }

    public void playWav(Uri uri) {
        try {
            mPlayer.initialize(uri, getApplicationContext());
            mPlayer.start();
        } catch (IOException e) {
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