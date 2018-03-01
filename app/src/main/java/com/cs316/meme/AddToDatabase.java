package com.cs316.meme;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;


/**
 * Created by user on 2/6/18.
 */

public class AddToDatabase extends AppCompatActivity{

    private static final String TAG = "AddToDatabase";

    private ImageView image;
    private EditText imageName;
    private Button btnUpload, btnNext, btnBack;
    private EditText topText, bottomText;
    private TextView  topTextView, bottomTextView;
    private RelativeLayout relativeLayout;

    private ProgressDialog mProgressDialog;

    private final static int mWidth = 512;
    private final static int mLength = 512;

    private ArrayList<String> pathArray;
    private int array_position;

    private StorageReference mStorageRef;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_database_layout);

        image = (ImageView) findViewById(R.id.uploadImage);
        btnBack = (Button) findViewById(R.id.btnBackImage);
        btnNext = (Button) findViewById(R.id.btnNextImage);
        btnUpload = (Button) findViewById(R.id.btnUploadImage);
        imageName = (EditText) findViewById(R.id.imageName);

        topText = (EditText) findViewById(R.id.topText);
        bottomText = (EditText) findViewById(R.id.bottomText);
        topTextView = (TextView) findViewById(R.id.topTextView);
        bottomTextView = (TextView) findViewById(R.id.bottomTextView);
        relativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout);



        pathArray = new ArrayList<>();
        mProgressDialog = new ProgressDialog(AddToDatabase.this);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        addFilePaths();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(array_position > 0){
                    Log.d(TAG, "onClick: Back an Image.");
                    array_position = array_position - 1;
                    loadImageFromStorage();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(array_position < pathArray.size() - 1){
                    Log.d(TAG, "onClick: Next Image.");
                    array_position = array_position + 1;
                    loadImageFromStorage();
                }
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Uploading Image.");
                mProgressDialog.setMessage("Uploading Image...");
                mProgressDialog.show();

                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();

                // Screenshots the image
                Bitmap viewBmp = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(), Bitmap.Config.ARGB_8888);
                viewBmp.setDensity(relativeLayout.getResources().getDisplayMetrics().densityDpi);
                Canvas canvas = new Canvas(viewBmp);
                relativeLayout.draw(canvas);
                String ImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        viewBmp,
                        "New Photo",
                        "New Image"

                );

                // Upload Image to firebase
                String name = imageName.getText().toString();
                if(!name.equals("")){
                    Uri uri = Uri.parse(ImageURL);
                    StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
                    storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            toastMessage("Upload Success");
                            mProgressDialog.dismiss();
                            addURL();
                            Intent intent = new Intent(AddToDatabase.this, Home.class);
                            startActivity(intent);
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

        // Editing text on Image
        topText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                topTextView.setText(topText.getText());
            }
        });
        bottomText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                bottomTextView.setText(bottomText.getText());
            }
        });
    }
    private void addURL(){
        FirebaseUser user = mAuth.getCurrentUser();
        final String userID = user.getUid();
        final String name = imageName.getText().toString();

        StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String downloadURL = uri.toString();
                Log.d(TAG,uri.toString());
                myRef.child(userID).child("ImageURL").child(name).setValue(downloadURL);
            }
        });

    }
    private void addFilePaths(){
        Log.d(TAG, "addFilePaths: Adding file paths.");

        String path = "/storage/emulated/0/DCIM/Camera";
        File file = new File(path, "");
        File[] listFile;

        if (file.isDirectory()){
            listFile = file.listFiles();

            for(int i=0; i < listFile.length; i++){
                pathArray.add(listFile[i].getAbsolutePath());
            }
        }

        loadImageFromStorage();

    }

    private void loadImageFromStorage() {

        if (pathArray.size() > 0) {
            try {
                String path = pathArray.get(array_position);
                File f = new File(path, "");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                Bitmap bb = Bitmap.createScaledBitmap(b, b.getWidth()/2, b.getHeight()/2, true);
                image.setImageBitmap(bb);
                image.setRotation(90);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "loadImageFromStorage: FileNotFoundException: " + e.getMessage());
            }
        }

        else {
            toastMessage("There are no photos");
        }


    }






    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
