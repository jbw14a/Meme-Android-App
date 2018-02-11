package com.cs316.meme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TakePhoto extends AppCompatActivity {

    private ImageView image;
    private static final int CAMERA_REQUEST = 123;
    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        image = (ImageView) findViewById(R.id.uploadImage);

    }

    public void takePhoto(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public void clear(View v){
        image.setImageBitmap(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            photo = (Bitmap) data.getExtras().get("data");
            String saveImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    photo,
                    "New Photo",
                    "New Image"

            );

            Uri savedImageURI = Uri.parse(saveImageURL);
            image.setImageURI(savedImageURI);

        }

    }
}
