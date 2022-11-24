package com.example.filterproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filterproject' library on application startup.
    static {
        System.loadLibrary("filterproject");
    }


    private ActivityMainBinding binding;

    public File file;
    Player mPlayer = new Player();

    FloatingActionButton pickAFileButton;
    Button plusButton;
    Button minusButton;
    float value250 = 0;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(

            new ActivityResultContracts.GetContent(),
            uri -> {

                this.file = new File(uri.getPath());
                playWav(uri, value250);
                String message = String.format(
                        "Reproduzindo %s com valor %s",
                        this.file.getPath(), this.value250
                );

                binding.sampleText.setText(message);
            });

    public void playWav(Uri uri, float value250) {
        try {
            mPlayer.initialize(uri, value250, getApplicationContext());
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
        plusButton = binding.plusButton;
        minusButton = binding.minusButton;

        plusButton.setOnClickListener(v -> {
            value250 += 0.1f;
            update250();
        });

        minusButton.setOnClickListener(v -> {
            value250 -= 0.1f;
            update250();
        });


        pickAFileButton.setOnClickListener(v -> {
            // start file chooser
            mGetContent.launch("audio/*");
        });

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

    }

    public void update250(){
        binding.value250.setText(String.valueOf(value250));
    }


    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */


}