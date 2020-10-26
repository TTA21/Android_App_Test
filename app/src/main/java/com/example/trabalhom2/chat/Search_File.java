package com.example.trabalhom2.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.R;
import com.example.trabalhom2.pass_utils.UriUtils;
import com.example.trabalhom2.structs.struct_url;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Search_File extends AppCompatActivity {

    class Audio_files{

        private MediaRecorder mediaRecorder = null;
        private String sound_file_path;
        private String audio_string = "";

        public MediaRecorder getMediaRecorder() {
            return mediaRecorder;
        }

        public void setMediaRecorder(MediaRecorder mediaRecorder) {
            this.mediaRecorder = mediaRecorder;
        }

        public String getSound_file_path() {
            return sound_file_path;
        }

        public void setSound_file_path(String sound_file_path) {
            this.sound_file_path = sound_file_path;
        }


        public String getAudio_string() {
            return audio_string;
        }

        public void setAudio_string(String audio_string) {
            this.audio_string = audio_string;
        }
    };


    private String this_user = "";
    private String table = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchfile);

        Bundle extra = getIntent().getExtras();

        if (extra != null) {

            this.this_user = extra.getString("this_user");
            this.table = extra.getString("table");

        }

    }

    private int CAMERA_PIC_REQUEST;
    private String base64_img = "";

    enum Action_type {
        CAMERA, AUDIO, FILE
    }

    private Action_type actionType;

    public void open_camera(View view) {

        actionType = Action_type.CAMERA;
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);

    }


    private Audio_files audio_files = new Audio_files();                                            ///Contains audio data
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void record_audio(View view) {

        actionType = Action_type.AUDIO;

        audio_files.setMediaRecorder( new MediaRecorder() );

        audio_files.getMediaRecorder().setAudioSource ( MediaRecorder.AudioSource.DEFAULT );
        audio_files.getMediaRecorder().setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);

        File sound_path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_MUSIC );
        File file = new File(sound_path , "/temp_storage.3gpp");

        audio_files.setSound_file_path( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_MUSIC ) + "/temp_storage.3gpp" );

        audio_files.getMediaRecorder().setOutputFile( file.getAbsolutePath() );
        audio_files.getMediaRecorder().setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );

        try {
            audio_files.getMediaRecorder().prepare();
            audio_files.getMediaRecorder().start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop_record(View view) {

        if( audio_files.getMediaRecorder() != null ){

            audio_files.getMediaRecorder().stop();
            audio_files.getMediaRecorder().release();
            audio_files.setMediaRecorder( null );

            File sound_file = new File( audio_files.getSound_file_path() );
            FileInputStream fileInputStreamReader = null;

            try {

                fileInputStreamReader = new FileInputStream( sound_file );
                byte[] bytes = new byte[(int)sound_file.length()];
                fileInputStreamReader.read(bytes);
                audio_files.setAudio_string( Base64.encodeToString(bytes, Base64.NO_PADDING) );

                deal_with_audio();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void search_file(View view) {

        actionType = Action_type.FILE;

        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);

    }

    private String Response = "";

    
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (actionType == Action_type.CAMERA) {

            base64_img = BitMapToString((Bitmap) data.getExtras().get("data"));

            deal_with_images();

        } else if (actionType == Action_type.FILE) {

            Uri selectedURI = data.getData(); //The uri with the location of the file

            String fullFilePath = UriUtils.getPathFromUri(this, selectedURI);
            File selectedFile = new File(fullFilePath);

            String filetype = MimeTypeMap.getFileExtensionFromUrl(fullFilePath);

            if (filetype.equals("jpg") || filetype.equals("png") || filetype.equals("bmp")) {   ///If image

                    try {

                        FileInputStream fileInputStreamReader = new FileInputStream(selectedFile);
                        byte[] bytes = new byte[(int)selectedFile.length()];
                        fileInputStreamReader.read(bytes);
                        base64_img = Base64.encodeToString(bytes, Base64.NO_PADDING);

                        deal_with_images();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("ERROR", "onActivityResult: ");
                    }

            }else if( filetype.equals("wav") || filetype.equals("mp3") || filetype.equals("3gpp") || filetype.equals("mpg") ){

                    try {

                        FileInputStream fileInputStreamReader = new FileInputStream(selectedFile);
                        byte[] bytes = new byte[(int)selectedFile.length()];
                        fileInputStreamReader.read(bytes);
                        audio_files.setAudio_string( Base64.encodeToString(bytes, Base64.NO_PADDING) );

                        deal_with_audio();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("ERROR", "onActivityResult: ");
                    }

            }

        }

        finish();
    }

    ///Expecting base64_img to already contain the data of the image
    public void deal_with_images() {

        struct_url url = new struct_url();
        final String url_s = url.getChat_send_message();

        Log.d("URL", "deal_with_images: " + url_s);
        Log.d("Table", "deal_with_images: " + table);
        Log.d("Base_64_img:", "onActivityResult: " + base64_img);

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 1 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the query was valid
            public void run() {
                new BackgroundTask().execute(url_s, table, "image" , base64_img);
            }
        }, 1000);   //1 seconds

        if (Response.equals("Message Sent")) {
            Toast.makeText(this, "Imagem Enviada", Toast.LENGTH_SHORT);
            Log.d("Status", "deal_with_images: " + "Message sent");
        }

    }

    public void deal_with_audio(){

        struct_url url = new struct_url();
        final String url_s = url.getChat_send_message();

        Log.d("URL", "deal_with_images: " + url_s);
        Log.d("Table", "deal_with_images: " + table);
        Log.d("audio_string:", "onActivityResult: " + audio_files.getAudio_string());

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 1 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the query was valid
            public void run() {
                new BackgroundTask().execute(url_s, table, "sound", audio_files.getAudio_string());
            }
        }, 1000);   //1 seconds

        if (Response.equals("Message Sent")) {
            Toast.makeText(this, "Imagem Enviada", Toast.LENGTH_SHORT);
            Log.d("Status", "deal_with_images: " + "Message sent");
        }

        finish();

    }

    public String BitMapToString(Bitmap bitmap){

        /// Reference https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
        /// Creator : sachin10
        /// Editor : tomrozb
        /// Acessdo em 20/10/2020 - 15:44

        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void go_back(View view) {

        if( audio_files.getMediaRecorder() != null ) {

            audio_files.getMediaRecorder().stop();
            audio_files.getMediaRecorder().release();
            audio_files.setMediaRecorder(null);

        }
        finish();

    }


    class BackgroundTask extends AsyncTask<String , Void , String>{

        String url_s = "";
        String table_name = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            this.url_s = params[0];
            this.table_name = params[1];

            try {
                URL url = new URL( url_s );
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String message_type = params[2];

                String data = URLEncoder.encode( "user" , "UTF-8" ) + "=" + URLEncoder.encode( this_user , "UTF-8" ) + "&";
                data += URLEncoder.encode( "table_name" , "UTF-8" ) + "=" + URLEncoder.encode( table_name , "UTF-8" ) + "&";
                data += URLEncoder.encode( "type" , "UTF-8" ) + "=" + URLEncoder.encode( message_type , "UTF-8" ) + "&";        ////Since this function only happens on the send text, it sends type 'text'
                data += URLEncoder.encode( "message" , "UTF-8" ) + "=" + URLEncoder.encode( params[3] , "UTF-8" );
                Log.d("Data: ", "doInBackground: " + data);
                bufferedWriter.write( data );
                bufferedWriter.flush();
                bufferedWriter.close();

                OS.close();

                InputStream IS = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( IS , "iso-8859-1" ));

                String line = "";

                while ( ( line = bufferedReader.readLine() ) != null ){
                    Response += line;
                }

                bufferedReader.close();

                IS.close();
                httpURLConnection.disconnect();

                return Response;

            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
