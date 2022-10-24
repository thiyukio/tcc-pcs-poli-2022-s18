package com.example.filterproject;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

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
                        uri);
                Log.d("myTag", message);
                this.file = new File(uri.getPath());
                playWav(uri);
                binding.sampleText.setText(message);
            });

    public void playWav(Uri uri) {
        mPlayer.initialize(uri, getApplicationContext());
        mPlayer.start();
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
    }

    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */

}