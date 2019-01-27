package com.iosm.chat;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.*;


public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageinput;


    private final List<Group_Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private Group_MessageAdapter messageAdapter ;
    private RecyclerView userMessageList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserId, currentDate, currentTime , currentUserName;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef =FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);




        InitilializeFields();

        GetUserinfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDb();
                userMessageinput.setText("");

            }
        });
    }


    public void SaveMessageToDb(){
        String message = userMessageinput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "please enter a message", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calforDate =Calendar.getInstance();
            SimpleDateFormat currentdateformat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentdateformat.format(calforDate.getTime());

            Calendar calforTime =Calendar.getInstance();
            SimpleDateFormat currenttimeformat = new SimpleDateFormat("hh:mm a");
            currentTime = currenttimeformat.format(calforTime.getTime());


            HashMap<String, Object> groupeMessagekey = new HashMap<>();
            GroupNameRef.updateChildren(groupeMessagekey);
            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String , Object> MessageInfoMap = new HashMap<>();
            MessageInfoMap.put("from", currentUserId);
            MessageInfoMap.put("name", currentUserName);
            MessageInfoMap.put("message", message);
            MessageInfoMap.put("date", currentDate);
            MessageInfoMap.put("time", currentTime);

            GroupMessageKeyRef.updateChildren(MessageInfoMap);





        }




    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Group_Messages messages = dataSnapshot.getValue(Group_Messages.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void GetUserinfo(){

        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void InitilializeFields(){
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageinput = (EditText) findViewById(R.id.input_group_message);


        messageAdapter = new Group_MessageAdapter(messageList);
        userMessageList = (RecyclerView) findViewById(R.id.group_messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
    }





}
