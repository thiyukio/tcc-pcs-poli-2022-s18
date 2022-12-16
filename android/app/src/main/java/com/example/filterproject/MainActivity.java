package com.example.filterproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.VolumeProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.net.Uri;

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
    ImageButton stopButton;
    Button[] plusButton = new Button[Player.NUM_BANDS];
    Button[] minusButton = new Button[Player.NUM_BANDS];
    int[] audiogram = { 0, 0, 0, 0, 0, 0 };

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(

            new ActivityResultContracts.GetContent(),
            uri -> {
                this.file = new File(uri.getPath());
                playAudio(uri, audiogram);
                binding.sampleText.setText("Reproduzindo!");
            });

    float volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pickAFileButton = binding.pickAFileButton;
        stopButton = binding.stopButton;
        stopButton.setVisibility(View.GONE);

        plusButton[0] = binding.plusButtonA;
        minusButton[0] = binding.minusButtonA;
        plusButton[1] = binding.plusButtonB;
        minusButton[1] = binding.minusButtonB;
        plusButton[2] = binding.plusButtonC;
        minusButton[2] = binding.minusButtonC;
        plusButton[3] = binding.plusButtonD;
        minusButton[3] = binding.minusButtonD;
        plusButton[4] = binding.plusButtonE;
        minusButton[4] = binding.minusButtonE;
        plusButton[5] = binding.plusButtonF;
        minusButton[5] = binding.minusButtonF;

        updateAudiogram();
        for (int i = 0; i < Player.NUM_BANDS; i++) {
            int finalI = i;
            plusButton[i].setOnClickListener(v -> {
                audiogram[finalI] += 1;
                updateAudiogram();
            });

            int finalI1 = i;
            minusButton[i].setOnClickListener(v -> {
                audiogram[finalI1] -= 1;
                updateAudiogram();
            });
        }

        pickAFileButton.setOnClickListener(v -> {
            // start file chooser
            mGetContent.launch("audio/*");
        });

        stopButton.setOnClickListener(v -> {
            mPlayer.stop();
            stopButton.setVisibility(View.GONE);
            for (int i = 0; i < Player.NUM_BANDS; i++) {
                plusButton[i].setEnabled(true);

                minusButton[i].setEnabled(true);
            }
            binding.sampleText.setText("Selecione uma música!");
        });

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                1);

    }

    public void playAudio(Uri uri, int[] audiogram) {
        try {
            Switch onSwitch = findViewById(R.id.switch1);
            mPlayer.initialize(uri, getApplicationContext(), audiogram, onSwitch);
            mPlayer.start();
            stopButton.setVisibility(View.VISIBLE);
            for (int i = 0; i < Player.NUM_BANDS; i++) {
                plusButton[i].setEnabled(false);

                minusButton[i].setEnabled(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateAudiogram() {
        binding.valueA.setText(String.format("%s dB", String.valueOf(audiogram[0])));
        binding.valueB.setText(String.format("%s dB", String.valueOf(audiogram[1])));
        binding.valueC.setText(String.format("%s dB", String.valueOf(audiogram[2])));
        binding.valueD.setText(String.format("%s dB", String.valueOf(audiogram[3])));
        binding.valueE.setText(String.format("%s dB", String.valueOf(audiogram[4])));
        binding.valueF.setText(String.format("%s dB", String.valueOf(audiogram[5])));
    }

    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */

}