package com.cs316.meme;

/**
 * Created by johnwolfe on 2/10/18.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class GalleryItem extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";
    private String imageUrl;
    private String imageName;
    private ShareDialog shareDialog;
    private CallbackManager callbackManager;
    private ShareButton fbButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Log.d(TAG, "onCreate: started.");

        if(getIntent().hasExtra("image_url") && getIntent().hasExtra("image_name")) {
            Log.d(TAG, "getIncomingIntent: found intent extras.");

            imageUrl = getIntent().getStringExtra("image_url");
            imageName = getIntent().getStringExtra("image_name");

            TextView name = findViewById(R.id.image_description);
            name.setText(imageName);

            ImageView image = findViewById(R.id.image);
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(image);
        }

        final Uri imageUri = Uri.parse(imageUrl);
        fbButton = (ShareButton) findViewById(R.id.facebookButton);

        SharePhoto photo = new SharePhoto.Builder()
                .setImageUrl(imageUri)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        fbButton.setShareContent(content);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ShareDialog.canShow(ShareLinkContent.class)){
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(imageUri)
                            .build();
                    shareDialog.show(linkContent);
                }
            }
        });
        //
//
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }



}


