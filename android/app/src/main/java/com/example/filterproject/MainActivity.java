package com.example.filterproject;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.view.View;

import com.example.filterproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filterproject' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;


    FloatingActionButton pickAFileButton;

    ImageButton playButton;

    Uri uri;
    ParcelFileDescriptor fd;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                String message = String.format(
                        "Consegui a uri = %s",
                        uri
                );
                this.uri = uri;
                Log.d("myTag", message);

                ParcelFileDescriptor inputPFD;
                try {
                    /*
                     * Get the content resolver instance for this context, and use it
                     * to get a ParcelFileDescriptor for the file.
                     */
                    inputPFD = getContentResolver().openFileDescriptor(uri, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "File not found.");
                    return;
                }
                // Get a regular file descriptor for the file
                this.fd = inputPFD;

                // Alterar o código restante do callback para outra coisa
//                try {
//                    func(uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                binding.sampleText.setText(message);
            });

    public void func(Uri uri) throws IOException {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
    }

    protected void onResume() {
        super.onResume();
        if(uri != null) {
//            if (SDK_INT >= Build.VERSION_CODES.R) {
//                try {
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                    intent.addCategory("android.intent.category.DEFAULT");
//                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
//                    startActivityForResult(intent, 2296);
//                } catch (Exception e) {
//                    Intent intent = new Intent();
//                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                    startActivityForResult(intent, 2296);
//                }
//                // native_onStart(this.fd.getFd());
//
//            }
            native_onResume();
        }
    }

    protected void onPause(){
        super.onPause();
        native_onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pickAFileButton = binding.pickAFileButton;
        playButton = binding.playButton;

        pickAFileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start file chooser
                mGetContent.launch("audio/*");
            }
        });

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        // tv.setText(Float.toString(test()[0]) + ", " + Float.toString(test()[1]));
        // tv.setText(test().toString());

        setDefaultStreamValues(this);
        native_onStart(getAssets());
    }

    static void setDefaultStreamValues(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            int defaultSampleRate = Integer.parseInt(sampleRateStr);
            String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);

            native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
        }
    }


    /**
     * A native method that is implemented by the 'filterproject' native library,
     * which is packaged with this application.
     */
    // public native float[] test();
    private static native void native_setDefaultStreamValues(int defaultSampleRate,
                                                             int defaultFramesPerBurst);
    private native void native_onStart(AssetManager assetManager);
    private native void native_onStop();

    private native void native_onResume();
}