package com.iosm.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar settings_toolbar;
    private EditText username, userStatus;
    private Button UpdateAccountSettings;
    private CircleImageView userProfileImage;
    private String CurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int gallerypic=1;

    private StorageReference UserProfileImgRef;
    private ProgressDialog laodingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        settings_toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        mAuth=FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImgRef = FirebaseStorage.getInstance().getReference().child("/Profile images");
        laodingbar = new ProgressDialog(this);


        InitializeFields();

        username.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });


        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallerypic );

            }
        });


    }





    public void UpdateSettings(){
        String SetUsername = username.getText().toString();
        String Setstatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(SetUsername)){
            Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
        }else  if(TextUtils.isEmpty(Setstatus)){
            Toast.makeText(this, "Please enter a valid Status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid" , CurrentUserId);
            profileMap.put("name" , SetUsername);
            profileMap.put("status" , Setstatus);

         RootRef.child("Users").child(CurrentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile updated Successfully", Toast.LENGTH_SHORT).show();



                    }else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " +message, Toast.LENGTH_SHORT).show();
                    }
             }
         });
        }

    }


    public void InitializeFields(){

        UpdateAccountSettings = (Button) findViewById(R.id.update_setting_button);
        username = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);


        setSupportActionBar(settings_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallerypic && resultCode == RESULT_OK && data != null){
            Uri imageuri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                laodingbar.setTitle("Set profile Image");
                laodingbar.setMessage("Please wait, your profile picture is updating... ");
                laodingbar.setCanceledOnTouchOutside(false);
                laodingbar.show();

                final Uri Resulturi = result.getUri();


                final StorageReference filepath= UserProfileImgRef.child(CurrentUserId + ".jpg");

                filepath.putFile(Resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            //final String downloadurl = task.getResult().getUploadSessionUri().
                           
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();



                            RootRef.child("Users").child(CurrentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this, "Image save in database successffuly", Toast.LENGTH_SHORT).show();

                                        laodingbar.dismiss();
                                    }else{
                                        String mssg= task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, mssg, Toast.LENGTH_SHORT).show();
                                        laodingbar.dismiss();
                                    }

                                }
                            });
                                }
                            });

                        } else {
                            String mssg=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error " +mssg, Toast.LENGTH_SHORT).show();
                            laodingbar.dismiss();
                        }

                    }
                });
            }

        }



    }

    public void RetrieveUserInfo(){
        RootRef.child("Users").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))  ){
                    String RetrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String RetrieveStatus=dataSnapshot.child("status").getValue().toString();
                    String RetrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                    username.setText(RetrieveUserName);
                    userStatus.setText(RetrieveStatus);
                    Picasso.get().load(RetrieveProfileImage).into(userProfileImage);


                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String RetrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String RetrieveStatus=dataSnapshot.child("status").getValue().toString();


                    username.setText(RetrieveUserName);
                    userStatus.setText(RetrieveStatus);

                }else {
                    username.setVisibility(View.VISIBLE );
                    Toast.makeText(SettingsActivity.this, "Please set and update your profile information ", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
