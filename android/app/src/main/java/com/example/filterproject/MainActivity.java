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
        int minBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_FLOAT);
        int bufferSize = 512;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT, minBufferSize, AudioTrack.MODE_STREAM);
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        int i = 0;
        byte[] s = new byte[bufferSize];
        try {
            FileInputStream fin = new FileInputStream(file);
            Log.d("nome2",file.getName());
            DataInputStream dis = new DataInputStream(fin);
            at.play();

            float float_amp[] = new float[(s.length)/4];
            for(int h = 0; h < float_amp.length; h++){
                float_amp[h] = 0.0f;
            }
            //float x[] = new float[20];
            //float h[]= {-0.0978f, 0.0877f, 0.0729f, 0.0698f, 0.0726f, 0.0780f, 0.0843f, 0.0897f, 0.0938f, 0.0961f, 0.0961f, 0.0938f, 0.0897f, 0.0843f, 0.0780f, 0.0726f, 0.0698f, 0.0729f, 0.0877f, -0.0978f};
            float[] a = {1f,-4.99391664112684f,18.1892784984545f,-45.6494594612107f,94.5430788481749f,-157.875718488687f,226.717832623222f,-274.691211854234f,290.541189408577f,-262.213294017987f,206.580020154715f,-137.292226253683f,78.4595443256522f,-36.1404979889858f,13.737332262346f,-3.59561454657024f,0.687019563911011f};
            float[] b = {0.0123627653610425f,-0.0576323731401262f,0.196131246510405f,-0.466800745736293f,0.926674024307069f,-1.50496550042397f,2.13002638772978f,-2.58286107703368f,2.77416410917846f,-2.58286107703368f,2.13002638772978f,-1.50496550042397f,0.926674024307069f,-0.466800745736293f,0.196131246510405f,-0.0576323731401261f,0.0123627653610425f};
            IIRFilter filter = new IIRFilter(a, b);
            //float h[]={-0.0930250949393319f, 0.0207057333964976f, 0.0291475759590533f, 0.0430501166124715f, 0.0601692514455458f, 0.0788068471187608f, 0.0968422736437664f, 0.112413676993475f, 0.123818015340460f, 0.129851026602985f, 0.129851026602985f, 0.123818015340460f, 0.112413676993475f, 0.0968422736437664f, 0.0788068471187608f, 0.0601692514455458f, 0.0430501166124715f, 0.0291475759590533f, 0.0207057333964976f, -0.0930250949393319f};
            //for(int ver = 0; ver < 300; ver++){
            //    i = dis.read(s, 0, bufferSize);
            //}

            while((i = dis.read(s, 0, bufferSize)) > -1){

                //filter.process(float_arr,float_amp);
                //at.write(float_arr,0,bufferSize,0);

                ShortBuffer sbuf =
                        ByteBuffer.wrap(s).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                short[] audioShorts = new short[sbuf.capacity()];
                sbuf.get(audioShorts);

                float[] audioFloats = new float[audioShorts.length/2];
                for (int t = 0; t < audioShorts.length/2; t++) {
                    audioFloats[t] = ((float)audioShorts[2*t])/0x8000;
                }

                filter.process(audioFloats,float_amp);
                /*
                Log.i("MyAndroidClass1", String.valueOf(audioFloats.length));
                Log.i("MyAndroidClass2", Byte.toString(s_amp[10]));
                for(int l =0; l< 256; l++){
                    for(int j = 0; j < 19; j++){
                        x[j]=x[j+1];
                    }
                    x[19] = audioFloats[l];
                    aux = 0.0f;
                    for(int k = 0; k < 20; k++){
                        aux += h[k]*x[19-k];
                    }
                    float_amp[l] = aux;
                }*/
                //Log.v("float_amp",String.valueOf(float_amp[5]));
                Log.i("MyAndroidClass", Arrays.toString(float_amp));
                at.write(float_amp, 0, 128, AudioTrack.WRITE_BLOCKING);

            }


            at.stop();
            at.release();
            dis.close();
            fin.close();

        } catch (FileNotFoundException e) {
            // TODO
            Log.d("erro",e.toString());
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
    public native float[] amplify(float[] input, float[
            ] input2, float[] h, int bufferSize);

    public class TTSInputStream extends DataInputStream {
        public TTSInputStream(InputStream in) {
            super(in);
        }

        public final int readFullyUntilEof(byte b[]) throws IOException {
            return readFullyUntilEof(b, 0, b.length);
        }

        public final int readFullyUntilEof(byte b[], int off, int len) throws IOException {
            if (len < 0)
                throw new IndexOutOfBoundsException();
            int n = 0;
            while (n < len) {
                int count = in.read(b, off + n, len - n);
                if (count < 0)
                    break;
                n += count;
            }
            return n;
        }
    }
}