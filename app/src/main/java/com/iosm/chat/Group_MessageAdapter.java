package com.iosm.chat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;


import java.util.List;

public class Group_MessageAdapter extends RecyclerView.Adapter<Group_MessageAdapter.MessageViewHolder> {

    private List<Group_Messages> groupMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;



    public Group_MessageAdapter(List<Group_Messages> groupMessageList){

        this.groupMessageList = groupMessageList;

    }




    public class MessageViewHolder extends RecyclerView.ViewHolder{


        public TextView SenderMessageText , ReceiverMessagesText;


        public MessageViewHolder(@NonNull View itemView){
            super(itemView);

            SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            ReceiverMessagesText = (TextView) itemView.findViewById(R.id.receiver_message_text);



        }


    }







    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
         View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_custom_messages_layout,viewGroup, false);

         mAuth = FirebaseAuth.getInstance();

         return  new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        String messageSenderId= mAuth.getCurrentUser().getUid();
        Group_Messages messages = groupMessageList.get(i);


        String fromUserID = messages.getFrom();
        String fromUserName = messages.getName();


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(messageSenderId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            messageViewHolder.ReceiverMessagesText.setVisibility(View.INVISIBLE);

            messageViewHolder.SenderMessageText.setVisibility(View.INVISIBLE);


            if(fromUserID.equals(messageSenderId)){

                messageViewHolder.SenderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.SenderMessageText.setBackgroundResource(R.drawable.send_messages_layout);
                messageViewHolder.SenderMessageText.setTextColor(Color.BLACK);

                String mssg = messages.getMessage();
                SpannableString redSpannable= new SpannableString(mssg);
                redSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, mssg.length(), 0);
                builder.append(redSpannable);

                messageViewHolder.SenderMessageText.setText(mssg+"\n" +messages.getDate()+"  "+messages.getTime());
            }

            else{


                messageViewHolder.ReceiverMessagesText.setVisibility(View.VISIBLE);

                messageViewHolder.ReceiverMessagesText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.ReceiverMessagesText.setTextColor(Color.BLACK);
                messageViewHolder.ReceiverMessagesText.setText(fromUserName +" :\n"+messages.getMessage()+"\n" +messages.getDate()+"  "+messages.getTime());




            }



    }

    @Override
    public int getItemCount() {



        return groupMessageList.size();

    }





}
