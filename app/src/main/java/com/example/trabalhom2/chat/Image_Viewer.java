package com.example.trabalhom2.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhom2.Login_Main;
import com.example.trabalhom2.R;
import com.example.trabalhom2.register.register_class;

public class Image_Viewer extends AppCompatActivity {

    private String base64_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        Bundle extras = getIntent().getExtras();

        if( extras != null ){

            this.base64_image = extras.getString( "image" );

        }

        display_image();

    }

    public void display_image(){

        try {
            byte[] encodeByte = Base64.decode(base64_image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

            ImageView img = (ImageView) findViewById( R.id.imageView );
            img.setImageBitmap( bitmap );

        } catch (Exception e) {
            e.getMessage();
            Toast.makeText( this , "Falha ao abrir a imagem" , Toast.LENGTH_SHORT );
        }

    }


    public void go_back(View view) {

        finish();
    }
}
