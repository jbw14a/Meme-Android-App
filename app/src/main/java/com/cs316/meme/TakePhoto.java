package com.cs316.meme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class TakePhoto extends AppCompatActivity {

    private static final String TAG = "TakePhoto";


    private ImageView image;
    private static final int CAMERA_REQUEST = 123;
    private Bitmap photo;
    private Button btnUpload;
    private EditText imageName;

    private ArrayList<String> pathArray;

    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgressDialog;

    private int array_position = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        image = (ImageView) findViewById(R.id.uploadImage);
        imageName = (EditText) findViewById(R.id.imageName);
        btnUpload = (Button) findViewById(R.id.btnUploadImage);
        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(TakePhoto.this);
        pathArray = new ArrayList<>();




        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Uploading Image.");
//                mProgressDialog.setMessage("Uploading Image...");
//                mProgressDialog.show();

                String name = imageName.getText().toString();

                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();

                if (!name.equals("")) {
                    Log.d(TAG, "Proceeding...");
                    Uri uri = Uri.fromFile(new File(pathArray.get(array_position)));
                    StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
                    storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            toastMessage("Upload Success");
                            mProgressDialog.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage("Upload Failed");
                            mProgressDialog.dismiss();
                        }
                    });


                }

            }
        });
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
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
