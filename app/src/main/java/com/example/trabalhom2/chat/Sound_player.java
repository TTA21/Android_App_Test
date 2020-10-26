package com.example.trabalhom2.chat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Sound_player extends AppCompatActivity {

    MediaPlayer sound = null;
    String file_path;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundplayer);

        Bundle extras = getIntent().getExtras();

        if( extras != null ) {

            file_path = extras.getString("file_path");

            Log.d("Data sent:", "onCreate: " + file_path);

        }

    }

    public void play(View view) {

        if( sound == null ){
            sound = new MediaPlayer();
            sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {

                sound.setDataSource( file_path );
                sound.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if( sound != null ){
            sound.start();
        }

    }

    public void pause(View view) {

        if( sound != null ){
            sound.pause();
        }

    }

    public void stop(View view) {

        if( sound != null ){
            sound.release();
            sound = null;
        }

    }

    public void Exit(View view) {

        if( sound != null ){
            sound.release();
            sound = null;
        }

        finish();

    }
}
