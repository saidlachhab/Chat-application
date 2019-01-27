package com.iosm.chat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View Privatechatview;
    private RecyclerView chatslist;

    private DatabaseReference chatsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;




    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Privatechatview= inflater.inflate(R.layout.fragment_chat, container, false);

        chatslist = (RecyclerView) Privatechatview.findViewById(R.id.chats_list);
        chatslist.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return Privatechatview;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                final String userIDs =getRef(position).getKey();
                final String[] retImage = {"default image"};

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("image")){
                                  retImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileImage);

                            }

                            final  String retName = dataSnapshot.child("name").getValue().toString();
                            final  String retStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(retName);



                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){
                                    holder.userStatus.setText("online");

                                }
                                else if(state.equals("offline")){
                                    holder.userStatus.setText("Last seen : " + date +" "+time);

                                }

                            }else{

                                holder.userStatus.setText("offline");

                            }




                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent =new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("Visit_user_id", userIDs);
                                    chatIntent.putExtra("Visit_image", retImage[0]);
                                    chatIntent.putExtra("Visit_user_name", retName);

                                    startActivity(chatIntent);
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                return new ChatsViewHolder(view);
            }
        };

        chatslist.setAdapter(adapter);
        adapter.startListening();

    }




    public static  class  ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userStatus,userName;



        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);

            userStatus = itemView.findViewById(R.id.user_status);





        }
    }
}
