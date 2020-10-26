package com.example.trabalhom2.register;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.Login_Main;
import com.example.trabalhom2.R;
import com.example.trabalhom2.pass_utils.Hash_Util;
import com.example.trabalhom2.structs.struct_url;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class register_class extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
    }

    public void go_back(View view) {                                                                                /// Sends the focus back into the previous class

        finish();

    }

    /// Gets the users inputs on the register_activity and attemts to write them onto the users table

    private String server_response = "";

    public void register_button(View view) {

        EditText name = (EditText) findViewById( R.id.register_name );
        String name_s = name.getText().toString();                                                          /// Get the input

            if( name_s.isEmpty() ){                                                                         /// Check if okay
                Toast.makeText( this , "Campo Nome está vazio!" , Toast.LENGTH_SHORT).show();
                return;                                                                                     /// If not, kill task
            }

        EditText email = (EditText) findViewById( R.id.register_email );
        String email_s = email.getText().toString();                                                        /// Get the input

            if( email_s.isEmpty() || !email_s.contains(".com") ){                                           /// Check if okay
                Toast.makeText( this , "Digite um email valido!" , Toast.LENGTH_SHORT).show();
                return;                                                                                     /// If not, kill task
            }

        EditText pass = (EditText) findViewById( R.id.register_pass );
        String pass_s = pass.getText().toString();                                                          /// Get the input

            if( pass_s.isEmpty() ){                                                                         /// Check if okay
                Toast.makeText( this , "Campo Senha está vazio!" , Toast.LENGTH_SHORT).show();
                return;                                                                                     /// If not, kill task
            }

        pass_s = Hash_Util.getSha256Hash( pass_s );                                                         /// Encrypt password

        new BackgroundTask().execute( name_s , email_s , pass_s );                                          /// Execute the register

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 2 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the register was valid
            public void run() {
                check_server_response();
            }
        }, 2000);   //2 seconds

    }

    public void check_server_response(){

        Toast.makeText( this , server_response , Toast.LENGTH_SHORT).show();

        if( server_response.contains("Succesful Registration") ) {

            ///Back to main

            ///https://stackoverflow.com/questions/12408719/resume-activity-in-android
            ///Autor: ThePCWizard , acessado em 09/10/2020 - 11:31
            ///Where you are , where you want to go
            Intent openMainActivity = new Intent(register_class.this, Login_Main.class);
            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);   ///Puts the desired class to the fron
            startActivityIfNeeded(openMainActivity, 0);

        }

    }


    class BackgroundTask extends AsyncTask<String , Void , String>{

        struct_url url = new struct_url();
        private String reg_url = url.getRegister_url();                                                     ///Holds the server url

        @Override
        protected String doInBackground(String... Params) {

            String Response = "";                                                                           /// Holds the server_response

            String name = Params[0];
            String email = Params[1];
            String pass = Params[2];                                                                        /// Password comes encrypted

            try {
                URL url = new URL( reg_url );                                                               /// Standard initialization method
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String data = URLEncoder.encode( "username" , "UTF-8" ) + "=" + URLEncoder.encode( name , "UTF-8" ) + "&";
                data += URLEncoder.encode( "email" , "UTF-8" ) + "=" + URLEncoder.encode( email , "UTF-8" ) + "&";
                data += URLEncoder.encode( "password" , "UTF-8" ) + "=" + URLEncoder.encode( pass , "UTF-8" );
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
                return Response;
            }

        }///DoinBackgroud

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }//onpreExecute

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }///onprogressupdate

        @Override
        protected void onPostExecute(String params) {
            server_response = params;
        }///onpostexecute

    } ///BackGround
}
