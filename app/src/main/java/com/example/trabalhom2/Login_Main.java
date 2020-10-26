package com.example.trabalhom2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.trabalhom2.after_login.After_login;
import com.example.trabalhom2.pass_utils.Hash_Util;
import com.example.trabalhom2.register.register_class;
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

public class Login_Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
                                                                                                    ///When it comes back , clear inputs
        EditText name = (EditText) findViewById(R.id.login_name);
        name.setText("");
        EditText pass = (EditText) findViewById(R.id.login_password);
        pass.setText("");
    }

    /// Takes the data on the activty and attempts to log in with a connection to the server

    private String query_result = "";

    public void user_login(View view) {

        EditText name = (EditText) findViewById( R.id.login_name );
        final String name_s = name.getText().toString();                                                    /// Get name from input

            if( name_s.isEmpty() ){                                                                         /// Check if it is okay
                Toast.makeText( this , "Campo Nome está vazio!" , Toast.LENGTH_SHORT).show();
                return;                                                                                     /// If not, kill task
            }

        EditText pass = (EditText) findViewById( R.id.login_password );
        String pass_s = pass.getText().toString();                                                          /// Get password from input

            if( pass_s.isEmpty() ){                                                                         /// Check if it is okay
                Toast.makeText( this , "Campo Senha está vazio!" , Toast.LENGTH_SHORT).show();
                return;                                                                                     /// If not kill tasks
            }

        pass_s = Hash_Util.getSha256Hash( pass_s );                                                         /// When everything checks out, encrypt password

        new BackgroundTask().execute( name_s , pass_s );                                                    /// Send the information to the server
                                                                                                            /// if it is accepted, it will echo the users id, it is useless for now
                                                                                                            /// otherwise it will send the string "Username or password incorrect"

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 0.5 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the login was valid
            public void run() {
                check_server_response( name_s );
            }
        }, 500);   //0.5 seconds

    }

    public void check_server_response( String name_s ){                                                     /// Checks if the server allowed the login

        if( query_result.contains("Username or password incorrect") ||                                      /// If the query returns "Username or password incorrect" , the user doenst match
                query_result.contains("Error connecting to the server")){                                   /// if it returns "Error connecting to the server" , the it probably lost connection
            Toast.makeText( this , query_result , Toast.LENGTH_SHORT).show();                       /// tell the user that the login data is incorrect
            query_result = "";                                                                              /// reset the query string
        }
        else{                                                                                               /// On the other hand, if it is accepted, open another activity [After_Login]
            Intent aft_log = new Intent( this , After_login.class );                         /// a new intent is created to pass the focus foward
            aft_log.putExtra("username" , name_s);                                                   /// and along with it, it is passed foward the username that just got logged in
            startActivity( aft_log );
            query_result = "";                                                                              /// [query_result] is reset, in case the user wants to log in with another user later
        }

    }

    public void set_to_register(View view) {

        startActivity( new Intent( getBaseContext() , register_class.class ));                              /// Opens the register activity

    }


    class BackgroundTask extends AsyncTask<String , Void , String>{

        struct_url url = new struct_url();                                                                  /// This is an object to the url structure
        private String log_url = url.getLogin_url() ;                                                       /// this variable stores the url for the login

        private String name;                                                                                /// The username found in activity input and passed by the function that called this class,
                                                                                                            /// this is global for it never changes
        private String pass;                                                                                /// this password is already encrypted , also passed by the function that called this class

        @Override
        protected String doInBackground(String... params) {                                                 /// Main function of this class

            String Response = "";

            this.name = params[0];                                                                          /// Getting data from the caller's parameters
            this.pass = params[1];

            try {

                URL url = new URL( log_url );                                                               /// Standard initiation sequence for this class ,
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();             /// it sets up a connection with the server
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String data = URLEncoder.encode( "username" , "UTF-8" ) + "=" + URLEncoder.encode( name , "UTF-8" ) + "&";      /// Parameters are passed by the url
                data += URLEncoder.encode( "password" , "UTF-8" ) + "=" + URLEncoder.encode( pass , "UTF-8" );                  /// since the password is already encrypted, "man in the middle attacks" are worthless
                bufferedWriter.write( data );
                bufferedWriter.flush();
                bufferedWriter.close();

                OS.close();

                InputStream IS = httpURLConnection.getInputStream();                                                                          /// This gets back the server response
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( IS , "iso-8859-1" ));

                String line = "";

                while ( ( line = bufferedReader.readLine() ) != null ){                                                                       /// Wait until the server responds, might take a while
                    Response += line;
                }

                bufferedReader.close();

                IS.close();
                httpURLConnection.disconnect();

                return Response;

            } catch (IOException e) {                                                                                                         /// In case there is an error , return the error code
                e.printStackTrace();
                return "Error connecting to the server , " + e.toString();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String param) {
            query_result = param;                                                                                                             ///Just send back the server response
        }
    }
}