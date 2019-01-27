package com.iosm.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewpager;
    private TabLayout MyTablayout;
    private TabsAccessorAdapter MyTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("OpenTalk");


        myViewpager = (ViewPager) findViewById(R.id.main_tabs_pager);
        MyTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewpager.setAdapter(MyTabsAccessorAdapter);

        MyTablayout = (TabLayout) findViewById(R.id.main_tabs);
        MyTablayout.setupWithViewPager(myViewpager);
    }


    @Override
    protected void onStart() {
        super.onStart();

FirebaseUser currentUser =mAuth.getCurrentUser();

        if(currentUser == null) {
            SendUserToLoginActivity();
        }else {

            updateUserStatus("online");
            VerifyUserExistance();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser =mAuth.getCurrentUser();


        if(currentUser !=null) {
            updateUserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser =mAuth.getCurrentUser();


        if(currentUser !=null) {
            updateUserStatus("offline");
        }
    }

    public void VerifyUserExistance(){
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else{
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }







    private void SendUserToLoginActivity(){

        Intent loginIntent = new Intent(MainActivity.this , LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    private void SendUserToSettingsActivity(){

        Intent SettingsIntent = new Intent(MainActivity.this , SettingsActivity.class);

        startActivity(SettingsIntent);


    }

    private void SendUserToFindFriendsActivity(){

        Intent FindFriendsIntent = new Intent(MainActivity.this , FindFriendsActivity.class);

        startActivity(FindFriendsIntent);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.option_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_Logout_option){

             updateUserStatus("offline");


             mAuth.signOut();
            SendUserToLoginActivity();
             Toast.makeText(this, "Logout successfuly", Toast.LENGTH_SHORT).show();
         }
        if(item.getItemId() == R.id.main_find_friends_option){
            SendUserToFindFriendsActivity();

        }
        if(item.getItemId() == R.id.main_Settings_option){
             SendUserToSettingsActivity();

        }

        if(item.getItemId() == R.id.main_create_group_option){
            RequestNewGroup();
        }

        return true;
    }



    public void RequestNewGroup(){
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Forum name : ");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("Ex: gaming Area");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please enter a Forum Name", Toast.LENGTH_SHORT).show();
                }else{
                    CreateNewGroup(groupName);
                }

            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();
    }


    public void CreateNewGroup(final String groupName){

        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+ " is Created Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void updateUserStatus(String state){

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar  = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object>  onlineStateMap  = new HashMap<>();
        onlineStateMap.put("time" , saveCurrentTime);
        onlineStateMap.put("date" , saveCurrentDate);
        onlineStateMap.put("state" , state);

        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);


    }

}
