package com.iosm.chat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestsFragementView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestsRef, UsersRef,ContactsRef;
    private FirebaseAuth mAuth;

    private String currentUserId;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragementView =inflater.inflate(R.layout.fragment_requests, container, false);
        myRequestList = (RecyclerView) RequestsFragementView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("chat requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        mAuth =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


    return RequestsFragementView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserId), Contacts.class).build();


        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder > adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                final String list_users_id = getRef(position).getKey();

                DatabaseReference getTypeRef =getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String type= dataSnapshot.getValue().toString();

                            if(type.equals("received")){

                                UsersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){


                                            final String requestprofileimage = dataSnapshot.child("image").getValue().toString();


                                            Picasso.get().load(requestprofileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);



                                        }

                                            final String requestUsername = dataSnapshot.child("name").getValue().toString();
                                            final String requestUserstatus = dataSnapshot.child("status").getValue().toString();

                                            holder.username.setText(requestUsername);
                                            holder.userstatus.setText("I want to connect with you");



                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence option[] = new CharSequence[]{

                                                        "Accept",
                                                        "Cancel"
                                                };
                                                 AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUsername +"Chat request");

                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i)
                                                    {
                                                        if (i == 0){

                                                            ContactsRef.child(currentUserId).child(list_users_id).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        ContactsRef.child(list_users_id).child(currentUserId).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){
                                                                                    ChatRequestsRef.child(currentUserId).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){

                                                                                                ChatRequestsRef.child(list_users_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        
                                                                                                        if (task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(), "New contact added", Toast.LENGTH_SHORT).show();
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

                                                                }
                                                            });



                                                        }
                                                        if (i == 1){

                                                            ChatRequestsRef.child(currentUserId).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){

                                                                        ChatRequestsRef.child(list_users_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                }


                                                                            }
                                                                        });
                                                                    }


                                                                }
                                                            });



                                                        }

                                                    }
                                                });
                                                builder.show();

                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            else if (type.equals("sent")){

                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_button);
                                request_sent_btn.setText("Request Sent");

                                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);










                                UsersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){


                                            final String requestprofileimage = dataSnapshot.child("image").getValue().toString();


                                            Picasso.get().load(requestprofileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);



                                        }

                                        final String requestUsername = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserstatus = dataSnapshot.child("status").getValue().toString();

                                        holder.username.setText(requestUsername);
                                        holder.userstatus.setText(" You have sent a request to : "+requestUsername);



                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence option[] = new CharSequence[]{

                                                        "Cancel chat request"
                                                };
                                                 AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already sent request");

                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {

                                                        if (which == 0){

                                                            ChatRequestsRef.child(currentUserId).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){

                                                                        ChatRequestsRef.child(list_users_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "You have cancel the chat request", Toast.LENGTH_SHORT).show();
                                                                                }


                                                                            }
                                                                        });
                                                                    }


                                                                }
                                                            });



                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });





            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
               View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
               RequestsViewHolder holder = new RequestsViewHolder(view);
               return holder;



            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class RequestsViewHolder extends RecyclerView.ViewHolder{


        TextView username,userstatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);


            username = itemView.findViewById(R.id.user_profile_name);
            userstatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            AcceptButton = itemView.findViewById(R.id.request_accept_button);
            CancelButton = itemView.findViewById(R.id.request_cancel_button);


        }
    }
}
