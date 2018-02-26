package com.cs316.meme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;

public class TakePhoto extends AppCompatActivity {


    private static final String TAG = "TakePhoto";
    private static final int CAMERA_REQUEST = 123;
    private ImageView image;
    private Button takePhotoBtn, uploadBtn;
    private EditText imageName, TopText, BottomText;
    private TextView TopTextView, BottomTextView;
    private RelativeLayout RLayout;


    private Bitmap photo;
    private Uri savedImageURI;

    private StorageReference mStorageRef;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        image = (ImageView) findViewById(R.id.uploadImage);
        takePhotoBtn = (Button) findViewById(R.id.takePhotoBtn);
        uploadBtn = (Button) findViewById(R.id.uploadImageBtn);
        imageName = (EditText) findViewById(R.id.imageName);
        TopText = (EditText) findViewById(R.id.TopText);
        BottomText = (EditText) findViewById(R.id.BottomText);
        TopTextView = (TextView) findViewById(R.id.TopTextView);
        BottomTextView = (TextView) findViewById(R.id.BottomTextView);
        RLayout = (RelativeLayout) findViewById(R.id.RelativeLayout);

        // Setup firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mProgressDialog = new ProgressDialog(TakePhoto.this);

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

        // Sned photo to firebase
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verify and get current user
                Log.d(TAG, "onClick: Uploading Image.");
                mProgressDialog.setMessage("Uploading Image...");
                mProgressDialog.show();

//----------------------------------------------------------
                Bitmap viewBmp = Bitmap.createBitmap(RLayout.getWidth(), RLayout.getHeight(), Bitmap.Config.ARGB_8888);
                viewBmp.setDensity(RLayout.getResources().getDisplayMetrics().densityDpi);
                Canvas canvas = new Canvas(viewBmp);
                RLayout.draw(canvas);
                String ImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        viewBmp,
                        "New Photo",
                        "New Image"

                );
                savedImageURI = Uri.parse(ImageURL);
                image.setImageURI(savedImageURI);
//----------------------------------------------------------

                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();


                // Get the name of the photo
                String name = imageName.getText().toString();

                if(!name.equals("")){
                    // The following two lines are what put the photo into firebase
                    StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
                    storageReference.putFile(savedImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            toastMessage("Upload Success");
                            mProgressDialog.dismiss();
                            addURL();
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

        TopText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                TopTextView.setText(TopText.getText());
            }
        });
        BottomText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BottomTextView.setText(BottomText.getText());
            }
        });

    }

    // ------ This is used to pull up the phone's camera --------
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

            savedImageURI = Uri.parse(saveImageURL);
            image.setImageURI(savedImageURI);

        }

    }

    // ---------------------------------------------------------------

    // Used above to send photo to Firebase storage. Repeats alot of the same code except for: myRef.child().child().child().setValue();
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

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
