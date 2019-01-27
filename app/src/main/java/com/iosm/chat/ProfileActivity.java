package com.iosm.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.HashMap;
import java.util.concurrent.Delayed;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserID,senderUserId,Current_state;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;
    private DatabaseReference UserRef, chatreqref, ContactsRef, NotificationsRef;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatreqref = FirebaseDatabase.getInstance().getReference().child("chat requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mAuth = FirebaseAuth.getInstance();

        receiverUserID =getIntent().getExtras().get("visit_user_id").toString();
        //Toast.makeText(this, "User ID "+receiverUserID, Toast.LENGTH_SHORT).show();

        senderUserId=mAuth.getCurrentUser().getUid();
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_user_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);


        Current_state="new";


        RetrieveUserInfo();


    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage= dataSnapshot.child("image").getValue().toString();
                    String userName= dataSnapshot.child("name").getValue().toString();
                    String userStatus= dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }else{

                    String userName= dataSnapshot.child("name").getValue().toString();
                    String userStatus= dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void ManageChatRequest() {

        chatreqref.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receiverUserID)){

                    String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent")){
                        Current_state = "request_sent";
                        SendMessageRequestButton.setText("Cancel chat request");
                    }

                    else if (request_type.equals("received")){
                        Current_state = "request_received";
                        SendMessageRequestButton.setText("Accept chat Request");

                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Cancelchatrequest();

                            }
                        });

                    }



                } else {
                    ContactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)){
                                Current_state="friends";
                                SendMessageRequestButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    if (!senderUserId.equals(receiverUserID)){

        SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageRequestButton.setEnabled(false);

                if(Current_state.equals("new")){

                    SendchatRequest();

                }
                if (Current_state.equals("request_sent")){
                    Cancelchatrequest();
                }
                if (Current_state.equals("request_received")){
                    Acceptchatrequest();
                }
                if (Current_state.equals("friends")){
                    RemoveSpecificContact();
                }

            }
        });


    }else{

        SendMessageRequestButton.setVisibility(View.INVISIBLE);
    }


    }






    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    ContactsRef.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                SendMessageRequestButton.setEnabled(true);
                                Current_state = "new";
                                SendMessageRequestButton.setText("Send message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });


    }

    private void Acceptchatrequest() {

        ContactsRef.child(senderUserId).child(receiverUserID).child("contacts").setValue("saved")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        ContactsRef.child(receiverUserID).child(senderUserId).child("contacts").setValue("saved")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                            chatreqref.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){
                                                        chatreqref.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                SendMessageRequestButton.setEnabled(true);
                                                                Current_state="friends";
                                                                SendMessageRequestButton.setText("Remove this Contact");
                                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                DeclineMessageRequestButton.setEnabled(false);



                                                            }
                                                        });
                                                    }


                                                }
                                            });

                                    }
                                });

                    }

                }
            });

    }


    private void SendchatRequest() {

        chatreqref.child(senderUserId).child(receiverUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    chatreqref.child(receiverUserID).child(senderUserId).child("request_type")
                            .setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        HashMap<String, String> chatnotificationMap = new HashMap<>();
                                        chatnotificationMap.put("from", senderUserId);
                                        chatnotificationMap.put("type", "request");
                                        NotificationsRef.child(receiverUserID).push()
                                                .setValue(chatnotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    SendMessageRequestButton.setEnabled(true);
                                                    Current_state = "request_sent";
                                                    SendMessageRequestButton.setText("Cancel chat request");

                                                }
                                            }
                                        });



                                    }
                                }
                            });
                }

            }
        });
    }


    private void Cancelchatrequest() {

        chatreqref.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    chatreqref.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                SendMessageRequestButton.setEnabled(true);
                                Current_state = "new";
                                SendMessageRequestButton.setText("Send message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });


    }
}
