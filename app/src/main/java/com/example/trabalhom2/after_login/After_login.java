package com.example.trabalhom2.after_login;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.chat.Chat_class;
import com.example.trabalhom2.Login_Main;
import com.example.trabalhom2.R;
import com.example.trabalhom2.structs.struct_url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;

public class After_login extends AppCompatActivity {

    private String this_username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_login_activity);

        update_user_spinner();                                                                      /// First thing, update spinner for user

        Bundle extras = getIntent().getExtras();
        if( extras != null){                                                                        /// Getting the username of this user
            this_username = extras.getString("username");
        }else{                                                                                      /// In case there is an error, return

            Toast.makeText( this , "Ocorreu um erro, voltando!" , Toast.LENGTH_SHORT).show();

            ///https://stackoverflow.com/questions/12408719/resume-activity-in-android
            ///Autor: ThePCWizard , acessado em 09/10/2020 - 11:31
                                                                ///Where you are , where you want to go
            Intent openMainActivity = new Intent(After_login.this, Login_Main.class);
            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);   ///Puts the desired class to the fron
            startActivityIfNeeded(openMainActivity, 0);

        }

    }

    String unparsed_JSON_users = "";
    ArrayList<String> parsed_users = null;

    public void update_user_spinner(){

        new BackgroundTask().execute("update_users");                                      /// Request a table of all users, they will be placed
                                                                                                    /// programatically in [user_spin]

        Handler handler = new Handler();                                                            /// It will wait for the servers response for 2 seconds
        handler.postDelayed(new Runnable() {                                                        /// then check if the request was valid
            public void run() {
                check_users_server_response();
            }
        }, 2000);   //1 seconds

    }

    public void check_users_server_response(){

        if (parsed_users != null){                                                                  /// if the call was passed correctly, there should be data here
            ArrayAdapter<String> adapter = new ArrayAdapter<String>( this,
                                                                        android.R.layout.simple_spinner_dropdown_item,
                                                                        parsed_users);              ///generates the spin list

            Spinner user_spin = (Spinner) findViewById(R.id.user_spinner);
            user_spin.setAdapter(adapter);                                                          ///updates spinner with the new list
        }else{
            Toast.makeText( this , "Erro , falha no servidor!" , Toast.LENGTH_SHORT );
        }

    }


    public void back_to_login(View view) {

        finish();

    }


    ArrayList<String[]> parsed_conversations = null;

    public void search_selected_user_history(View view) {

            Spinner user_spinner = (Spinner) findViewById(R.id.user_spinner);

            if ( user_spinner.getSelectedItem() != null) {                                          /// If spinner is null, user needs to execute search query

                new BackgroundTask().execute("search_conversation_tables");                ///This is going to update the conversation spinner

                Handler handler = new Handler();                                                                    /// It will wait for the servers response for 1 seconds
                handler.postDelayed(new Runnable() {                                                                /// then check if the register was valid
                    public void run() {
                        update_conversation_spinner();
                    }
                }, 1000);   //1 seconds

            }else{

                Toast.makeText( this , "Erro, nenhum usuario encontrado" , Toast.LENGTH_SHORT).show();

            }




    }

    public void update_conversation_spinner(){

        if (parsed_conversations != null){

        ArrayList<String> streamlined_conversation = new ArrayList<String>() ;

        for( int I = 0 ; I < parsed_conversations.size() ; I++ ){

            streamlined_conversation.add( parsed_conversations.get(I)[1] );

        }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    streamlined_conversation);                                                      ///generates the spin list

            Spinner conv_spinner = (Spinner) findViewById(R.id.conversation_spinner);
            conv_spinner.setAdapter(adapter);                                                       ///updates spinner with the new list
        }

    }

    public void access_chat(View view) {        ///Acess the chat selected on the second spinner

        Spinner conv_spinner = (Spinner) findViewById( R.id.conversation_spinner );

        if( conv_spinner.getSelectedItem() != null ){

            Intent chat_intent = new Intent( this , Chat_class.class );
            chat_intent.putExtra("this_user" , this_username);

            Spinner user_spinner = (Spinner) findViewById( R.id.user_spinner );
            String friend = user_spinner.getSelectedItem().toString();
            chat_intent.putExtra("friend" , friend );


            int Index = conv_spinner.getSelectedItemPosition();
            String selected_conversation = parsed_conversations.get(Index)[0];
            chat_intent.putExtra("table_name" , selected_conversation );

            startActivity( chat_intent );

        }else{

            Toast.makeText( this , "É necessário escolher um chat primeiro" , Toast.LENGTH_SHORT).show();

        }

    }

    String generated_table_name = "";

    public void create_chat(View view) {                                                            /// Take this user and selected user and make a table, then access it

        Spinner user_spinner = (Spinner) findViewById( R.id.user_spinner );
        final String friend = user_spinner.getSelectedItem().toString();                            /// The other person in the chat

        Toast.makeText( this , "Criando chat com " + friend , Toast.LENGTH_SHORT).show();

        new BackgroundTask().execute("create_conversation");

        final Context ctx = this;

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 3 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the register was valid
            public void run() {

                if( generated_table_name != "" ){
                    set_chat( friend );
                }else{
                    Toast.makeText( ctx , "Falha ao criar chat" , Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);   //3 seconds

    }

    public void set_chat( String friend ){

        Intent chat_intent = new Intent( this , Chat_class.class );
        chat_intent.putExtra("this_user" , this_username);
        chat_intent.putExtra("friend" , friend );
        chat_intent.putExtra("table_name" , generated_table_name );

        startActivity( chat_intent );

    }

    class BackgroundTask extends AsyncTask<String , Void , Void>{

            struct_url url = new struct_url();
            String get_unp_JSON_usr_url = url.getAft_log_get_all_users();
            String get_unp_conv_tables = url.getAft_log_search_conv_tables();
            String create_conv_table = url.getAft_log_create_conv_table();

            @Override
            protected Void doInBackground(String... strings) {

                String method = strings[0]; ////Method that dictates what to do in this task

                if( method.equals("update_users") ){

                    unparsed_JSON_users = get_unparsed_JSON_users( get_unp_JSON_usr_url );          ///Get raw JSON
                    if( !unparsed_JSON_users.isEmpty() ){
                        parsed_users = parseJSON_users( unparsed_JSON_users );                      ///Turn raw JSON into a array list for further use
                    }else{
                        return null;                                                                /// If connection is lost, it will be empty
                    }

                }else if( method.equals("search_conversation_tables") ){                            ///Find all tables that reference this user, and another selected by spinner

                    Spinner user_spinner = (Spinner) findViewById( R.id.user_spinner );
                    String friend = user_spinner.getSelectedItem().toString();    ///what to look for

                    String unparsed_conversations = get_unparsed_JSON_conversations( this_username , friend , get_unp_conv_tables );

                    parsed_conversations = parseJSON_conversations_table( unparsed_conversations );

                }else if( method.equals("create_conversation") ){

                    Spinner user_spinner = (Spinner) findViewById( R.id.user_spinner );
                    String friend = user_spinner.getSelectedItem().toString();    ///what to look for

                    generated_table_name = create_conversation_table( this_username , friend , create_conv_table );

                    Log.d("Generated Table:", "doInBackground: " + generated_table_name);

                }

                return null;
            }

            private String get_unparsed_JSON_users( String url_s ){

                String Response = "";
                try {
                    URL url = new URL( url_s );
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

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
                    return "";                                                                      /// If there is an error, cancel everything
                }
            }

            private ArrayList<String> parseJSON_users(String unparsed_string ){

                ArrayList<String> parsed = new ArrayList<>();

                JSONObject jsonObject = null;       ///Object created from string
                JSONArray jsonArray;                ///This contains all the useful data

                try {

                    jsonObject = new JSONObject( unparsed_string );
                    jsonArray = jsonObject.getJSONArray("server_response");

                    for( int I = 0 ; I < jsonArray.length() ; I++ ){

                        JSONObject JO = jsonArray.getJSONObject(I); ///Index of the mysql stuff
                        parsed.add( JO.getString("username") );

                    }

                    return parsed;


                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            private String get_unparsed_JSON_conversations(String user1 , String user2 , String url_s){

                String Response = "";
                try {
                    URL url = new URL( url_s );
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                    String data = URLEncoder.encode( "username1" , "UTF-8" ) + "=" + URLEncoder.encode( user1 , "UTF-8" ) + "&";
                    data += URLEncoder.encode( "username2" , "UTF-8" ) + "=" + URLEncoder.encode( user2 , "UTF-8" );
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

            }

            private ArrayList<String[]> parseJSON_conversations_table(String unparsed_string){

                ArrayList<String[]> parsed = new ArrayList<>();

                JSONObject jsonObject = null;       ///Object created from string
                JSONArray jsonArray;                ///This contains all the useful data

                try {

                    jsonObject = new JSONObject( unparsed_string );
                    jsonArray = jsonObject.getJSONArray("server_response");

                    for( int I = 0 ; I < jsonArray.length() ; I++ ){

                        JSONObject JO = jsonArray.getJSONObject(I); ///Index of the mysql stuff
                        String[] midway_raw = new String[2];
                        midway_raw[0] = JO.getString("Tables_in_trabalhom2");   ///raw data

                        StringBuilder str = new StringBuilder( JO.getString("Tables_in_trabalhom2") );
                        StringBuilder finished_str = new StringBuilder("");

                        int startIdx;
                        int endIdx;

                        startIdx = 0;
                        endIdx = str.indexOf("_");

                        finished_str.append( str.substring( startIdx , endIdx ) + " e " );
                        str.replace(startIdx, endIdx + 1, "");

                        startIdx = 0;
                        endIdx = str.indexOf("_");

                        finished_str.append( str.substring( startIdx , endIdx ) + " , " );
                        str.replace(startIdx, endIdx + 1, "");

                            ///Remove minutes
                            startIdx = str.indexOf("s");
                            endIdx = str.indexOf("_d");

                            str.replace(startIdx, endIdx, "");
                            ///Remove minutes

                        startIdx = str.indexOf("_d");
                        endIdx = str.indexOf("_m");

                        finished_str.append( str.substring( startIdx + 2 , endIdx ) + "/" );
                        str.replace(startIdx, endIdx + 1, "");

                        startIdx = str.indexOf("m");
                        endIdx = str.indexOf("_y");

                        finished_str.append( str.substring( startIdx + 1 , endIdx ) + "/" );
                        str.replace(startIdx, endIdx + 1, "");

                        startIdx = str.indexOf("y");
                        endIdx = str.length();

                        finished_str.append( str.substring( startIdx+1 , endIdx ) + " - " );

                                ///Get hour

                                str = new StringBuilder( JO.getString("Tables_in_trabalhom2") );

                                startIdx = str.indexOf("_h");
                                endIdx = str.indexOf("_d");

                                finished_str.append( str.substring( startIdx+2 , endIdx ) + ":" );

                                startIdx = str.indexOf("_m");
                                endIdx = str.indexOf("_h");

                                finished_str.append( str.substring( startIdx+2 , endIdx ) + ":" );

                                startIdx = str.indexOf("_s");
                                endIdx = str.indexOf("_m");

                                finished_str.append( str.substring( startIdx+2 , endIdx ) );

                                ///Get Hour

                        midway_raw[1] = finished_str.toString();

                        parsed.add(midway_raw);

                    }

                    return parsed;


                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            private String create_conversation_table( String user1 , String user2 , String url_s ){

                String Response = "";
                try {
                    URL url = new URL( url_s );
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                    String data = URLEncoder.encode( "user1" , "UTF-8" ) + "=" + URLEncoder.encode( user1 , "UTF-8" ) + "&";
                    data += URLEncoder.encode( "user2" , "UTF-8" ) + "=" + URLEncoder.encode( user2 , "UTF-8" );
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
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }


}
