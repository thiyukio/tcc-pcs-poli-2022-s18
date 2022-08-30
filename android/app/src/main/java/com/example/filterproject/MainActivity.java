package com.example.filterproject;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filterproject' library on application startup.
    static {
        System.loadLibrary("filterproject");
    }

    private ActivityMainBinding binding;


    FloatingActionButton pickAFileButton;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                String message = String.format(
                        "Consegui a uri = %s",
                        uri
                );
                Log.d("myTag", message);

                // Alterar o código restante do callback para outra coisa
                binding.sampleText.setText(message);
            });

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
        tv.setText(Float.toString(test()[0]) + ", " + Float.toString(test()[1]));
        // tv.setText(test().toString());
    }



    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */
    public native float[] test();
}