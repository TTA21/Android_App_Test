package com.example.trabalhom2.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.R;
import com.example.trabalhom2.structs.struct_url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat_class extends AppCompatActivity {

    String this_user = "";
    String friend = "";
    String table_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Bundle extras = getIntent().getExtras();

        if( extras != null ){

            this.this_user = extras.getString( "this_user" );
            this.friend = extras.getString( "friend" );
            this.table_name = extras.getString( "table_name" );

        }

        update_list_View();
        startTimer();

    }

    public void go_back(View view) {

        stopTimer();
        finish();

    }

    String unparsed_conv_JSON = "";
    ArrayList<String[]> parsed_conversation = null;
    public void update_list_View(){

        struct_url url = new struct_url();
        String url_s = url.getChat_get_conversations_url();

        new BackgroundTask( "get_conversations" ).execute( url_s , table_name );

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 0.5 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the query was valid
            public void run() {
                set_list();
            }
        }, 500);   //0.5 seconds

    }




    ListView listView;
    public void set_list(){

        if( parsed_conversation != null ){
            listView = (ListView) findViewById(R.id.list_View);
            ArrayList<String> display_messages = setup_parsed_conversation();
            ArrayAdapter arrayAdapter = new ArrayAdapter( this , android.R.layout.simple_list_item_1 , display_messages );
            listView.setAdapter( arrayAdapter );
            listView.setOnItemClickListener( listClick );
        }

    }

    /// When the user selects something in the list, do something
    private AdapterView.OnItemClickListener listClick = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String type = parsed_conversation.get(position)[3];

            if( type.equals("text") ){
                deal_with_text();
            }else if ( type.equals("image") ){
                deal_with_image( position );
            }else if( type.equals("sound") ){
                deal_with_sound( position );
            }

        }
    };

    public void deal_with_text(){
        Log.d("Message type: ", "deal_with_text: " + "TEXT");
    }

    public void deal_with_image( int position ){
        Log.d("Message type: ", "deal_with_text: " + "IMAGE");

        //startActivity( new Intent( getBaseContext() , Image_Viewer.class ));
        Intent img_intent = new Intent( this , Image_Viewer.class );

        String image = parsed_conversation.get(position)[2];
        img_intent.putExtra("image" , image);

        startActivity(img_intent);

    }

    public void deal_with_sound( int position ){

        Log.d("Message type: ", "deal_with_text: " + "SOUND");

        Intent snd_intent = new Intent( this , Sound_player.class );

        String sound = parsed_conversation.get(position)[2];
        //snd_intent.putExtra("sound" , sound);                                                     ///Unfortunately, if a sound file is too big, it cant be
                                                                                                    ///sent as a string, it will instead be created as a file and its url sent

        byte[] sound_byte = sound.getBytes();
        sound_byte = Base64.decode(sound_byte, Base64.DEFAULT);
        InputStream sound_stream = new ByteArrayInputStream(sound_byte);

        String file_path = create_file(sound_stream);
        snd_intent.putExtra( "file_path" , file_path );

        startActivity(snd_intent);

    }
    ///Used to create sound file only
    public String create_file( InputStream inputStream ){
        String pathname = this.getFilesDir() + File.separator + "temp_sound_file.txt";
        File file = new File(pathname);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte buf[]=new byte[1024];
            int len;
            while((len=inputStream.read(buf))>0) {
                fileOutputStream.write(buf,0,len);
            }

            Log.d("File abs path:", "create_file: " + file.getAbsolutePath());

            fileOutputStream.close();
            inputStream.close();
            Log.d("Finished creating file", "create_file: ");
        } catch (IOException e1) {}

        return pathname;

    }


    private ArrayList<String> setup_parsed_conversation(){  ///pretty up te message

        ArrayList<String> arrayList = new ArrayList<String>();

        for( int I = 0 ; I < parsed_conversation.size() ; I++ ){

            String store = parsed_conversation.get(I)[0] + " , "; ///Nome
            store += parsed_conversation.get(I)[1] + " : "; ///data ou horario

            if( parsed_conversation.get(I)[3].contains("text") ){
                store += "\n\t" + parsed_conversation.get(I)[2]; ///Conteudo da mensagem
            }else if ( parsed_conversation.get(I)[3].contains("image") ){
                store += "\n\t Toque para ver a imagem";
            }else if( parsed_conversation.get(I)[3].contains("sound") ){
                store += "\n\t Toque para ouvir a mensagem";
            }

            arrayList.add( store );

        }

        return arrayList;

    }

            ////Every 3 seconds the checksu of the table will be checked, if different, update list
            private Timer timer;
            private TimerTask timerTask;
            private Handler handler = new Handler();

            private long old_checksum = 0;
            private long new_checksum = 0;
            private long return_check = 0;   ///what comes back from server

            //To stop timer
            private void stopTimer(){
                if(timer != null){
                    timer.cancel();
                    timer.purge();
                }
            }

            //To start timer
            private void startTimer(){
                timer = new Timer();
                timerTask = new TimerTask() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run(){
                                ////Check sum of table

                                old_checksum = new_checksum;
                                new_checksum =  get_checksum();

                                if( old_checksum != new_checksum ){
                                    return_check = 0;
                                    update_list_View();
                                }

                            }
                        });
                    }
                };
                timer.schedule(timerTask, 3000, 3000);
            }



    private long get_checksum(){

        Handler handler = new Handler();                                                                    /// It will wait for the servers response for 0.5 seconds
        handler.postDelayed(new Runnable() {                                                                /// then check if the query was valid
            public void run() {
                struct_url url = new struct_url();
                String url_s = url.getChat_get_checksum();
                new BackgroundTask( "get_checksum" ).execute( url_s , table_name );
            }
        }, 200);   //0.2 seconds

        return return_check;

    }

    String message_debug = "";  ///expecting return from server;

    public void send_msg(View view) {

        ///get message from msg_box and send it, along with username and NOT time

        final EditText message_box = (EditText)findViewById( R.id.chat_message_box );
        String message = message_box.getText().toString();

        if( !message.isEmpty() ){

            struct_url url = new struct_url();
            String url_s = url.getChat_send_message();

            new BackgroundTask( "send_message" ).execute( url_s , table_name , this_user , message );

            hideKeyboard( this );

            Handler handler = new Handler();                                                                    /// It will wait for the servers response for 0.5 seconds
            handler.postDelayed(new Runnable() {                                                                /// then check if the query was valid
                public void run() {
                    message_afterath( message_box );
                }
            }, 500);   //0.5 seconds
        }else{
            Toast.makeText( this , "Erro ao enviar mensagem" , Toast.LENGTH_SHORT ).show();
        }

    }

    private void message_afterath( EditText message_box ){

        Toast.makeText( this , message_debug , Toast.LENGTH_SHORT  );
        message_debug = ""; ///Reset params

        update_list_View();
        message_box.setText("");

    }

    public static void hideKeyboard(Activity activity) {

        ///Reference : https://stackoverflow.com/questions/1109022/how-do-you-close-hide-the-android-soft-keyboard-using-java/53077131#53077131
        ///Author : Khemraj , accessed in 17/10/2020 , 15:05

        View v = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null && v != null;
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public void search_img(View view) {

        Intent srch_img_intent = new Intent( this , Search_File.class );

        srch_img_intent.putExtra( "this_user" , this_user );
        srch_img_intent.putExtra( "table" , table_name );

        startActivity( srch_img_intent );

    }


    class BackgroundTask extends AsyncTask<String , Void , String>{

        String url_s = "";
        String table_name = "";
        String method = "";

        BackgroundTask( String method ){
            this.method = method;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            this.url_s = params[0];
            this.table_name = params[1];

            if( method.equals("get_conversations") ){

                unparsed_conv_JSON = get_unparsed_JSON_conversation();
                parsed_conversation = parse_conversation( unparsed_conv_JSON );

            }else if( method.equals("get_checksum") ){

                return_check = get_checksum_from_table();

            }else if( method.equals("send_message") ){

                String username = params[2];
                String message = params[3];

                message_debug = send_message( username , message );

            }
            return null;
        }

        private String get_unparsed_JSON_conversation(){

            String Response = "";
            try {
                URL url = new URL( url_s );
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String data = URLEncoder.encode( "table_name" , "UTF-8" ) + "=" + URLEncoder.encode( table_name , "UTF-8" ) + "&";
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

        private ArrayList<String[]> parse_conversation( String unparsed_JSON ){

            ArrayList<String[]> parsed = new ArrayList<>();

            JSONObject jsonObject = null;       ///Object created from string
            JSONArray jsonArray;                ///This contains all the useful data

            try {

                jsonObject = new JSONObject( unparsed_JSON );
                jsonArray = jsonObject.getJSONArray("server_response");

                for( int I = 0 ; I < jsonArray.length() ; I++ ){

                    JSONObject JO = jsonArray.getJSONObject(I); ///Index of the mysql stuff

                    String[] midway = new String[4];

                    midway[0] = JO.getString("line_author");
                    midway[1] = JO.getString("time_of_writing").replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(""));
                    midway[2] = JO.getString("written");
                    midway[3] = JO.getString("type");

                    parsed.add(midway);

                }

                return parsed;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        private long get_checksum_from_table(){

            String Response = "";
            try {
                URL url = new URL( url_s );
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String data = URLEncoder.encode( "table_name" , "UTF-8" ) + "=" + URLEncoder.encode( table_name , "UTF-8" ) + "&";
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

                return Long.parseLong(Response);

            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }


        }

        private String send_message( String username , String message ){

            String Response = "";
            try {
                URL url = new URL( url_s );
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( OS , "UTF-8"));

                String message_type = "text";

                String data = URLEncoder.encode( "user" , "UTF-8" ) + "=" + URLEncoder.encode( username , "UTF-8" ) + "&";
                data += URLEncoder.encode( "message" , "UTF-8" ) + "=" + URLEncoder.encode( message , "UTF-8" ) + "&";
                data += URLEncoder.encode( "table_name" , "UTF-8" ) + "=" + URLEncoder.encode( table_name , "UTF-8" ) + "&";
                data += URLEncoder.encode( "type" , "UTF-8" ) + "=" + URLEncoder.encode( message_type , "UTF-8" );        ////Since this function only happens on the send text, it sends type 'text'
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
