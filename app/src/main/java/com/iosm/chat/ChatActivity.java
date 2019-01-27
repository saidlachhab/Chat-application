package com.iosm.chat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolbar;
    private FirebaseAuth mAuth;

    private ImageButton SendMessageButton;
    private ImageButton videocall;
    private EditText MessageInputText;
    private DatabaseReference RootRef, NotificationsVideoRef;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter ;
    private RecyclerView userMessageList;
    public static int val = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("Visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("Visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("Visit_image").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderID =mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        NotificationsVideoRef = FirebaseDatabase.getInstance().getReference().child("NotificationsVideo");

        InitiliazeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        videocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Videocall();

            }
        });
    }

    public void InitiliazeControllers(){


        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        videocall = (ImageButton) findViewById(R.id.video_call);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        SendMessageButton = (ImageButton) findViewById(R.id.send_mssg_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);



        messageAdapter = new MessageAdapter(messageList);
        userMessageList = (RecyclerView) findViewById(R.id.private_messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        DisplayLastseen();

    }

    private void DisplayLastseen(){
        RootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online")){
                        userLastSeen.setText("online");

                    }
                    else if(state.equals("offline")){
                        userLastSeen.setText(" " + date +" "+time);

                    }

                }else{

                    userLastSeen.setText("offline");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
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

    private void sendMessage(){

        String messageText = MessageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please enter a message .. ", Toast.LENGTH_SHORT).show();
        }else{

            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderRef)
                    .child(messageReceiverRef).push();

            String messagePushID= userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "message sent...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "Message not sent", Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText("");
                }
            });


        }

    }



    private void Videocall(){


            RootRef.child("valeur").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                         String vals = dataSnapshot.getValue().toString();
                         val = Integer.parseInt(vals);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        String tunnell = "";

            tunnell = "videocall" + (val+1);



        Intent videointent = new Intent(getBaseContext(), VideoActivity.class);
        videointent.putExtra("tunnel" , tunnell);
        startActivity(videointent);

        Toast.makeText(this, messageReceiverID +" et "+messageSenderID , Toast.LENGTH_SHORT).show();


        RootRef.child("valeur").setValue(val+1);



        HashMap<String, String> chatnotificationvMap = new HashMap<>();
        chatnotificationvMap.put("from", messageSenderID);
        chatnotificationvMap.put("type", "videocall");
        NotificationsVideoRef.child(messageReceiverID).push()
                .setValue(chatnotificationvMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    Toast.makeText(ChatActivity.this, "mzyan l video", Toast.LENGTH_SHORT).show();


                }
            }
        });
    }

}
