package com.example.filterproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    Button[] plusButton = new Button[Player.NUM_BANDS];
    Button[] minusButton = new Button[Player.NUM_BANDS];
    float value250 = 0;
    int[] audiogram = { 0, 0, 0, 0, 0, 0 };

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(

            new ActivityResultContracts.GetContent(),
            uri -> {

                this.file = new File(uri.getPath());
                playWav(uri, value250);
                String message = String.format(
                        "Reproduzindo %s com valor %s",
                        this.file.getPath(), this.value250);

                binding.sampleText.setText(message);
            });

    public void playWav(Uri uri, float value250) {
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


        for (int i=0; i < Player.NUM_BANDS; i++) {
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

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                1);

    }

    public void updateAudiogram() {
        binding.valueA.setText(String.valueOf(audiogram[0]));
        binding.valueB.setText(String.valueOf(audiogram[1]));
        binding.valueC.setText(String.valueOf(audiogram[2]));
        binding.valueD.setText(String.valueOf(audiogram[3]));
        binding.valueE.setText(String.valueOf(audiogram[4]));
        binding.valueF.setText(String.valueOf(audiogram[5]));
    }

    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */

}