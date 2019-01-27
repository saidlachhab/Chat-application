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
import android.widget.ImageView;
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
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView= inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();

        CurrentUserId =mAuth.getCurrentUser().getUid();

        ContactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(CurrentUserId);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class).build();


        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                final String UserIds = getRef(position).getKey();


                final String[] retImage = {"default image"};






                UsersRef.child(UserIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {


                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileimage);

                            }

                            final String retName = dataSnapshot.child("name").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();
                            holder.username.setText(retName);
                            holder.userstatus.setText(retStatus);


                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);

                                } else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);


                                }

                            } else {

                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                            }


                            if (dataSnapshot.hasChild("image")) {
                                String userimage = dataSnapshot.child("image").getValue().toString();
                                String profilname = dataSnapshot.child("name").getValue().toString();
                                String profilstatus = dataSnapshot.child("status").getValue().toString();


                                holder.username.setText(profilname);
                                holder.userstatus.setText(profilstatus);
                                Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(holder.profileimage);
                            } else {
                                String profilname = dataSnapshot.child("name").getValue().toString();
                                String profilstatus = dataSnapshot.child("status").getValue().toString();

                                holder.username.setText(profilname);
                                holder.userstatus.setText(profilstatus);

                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_user_id", UserIds);
                                    chatIntent.putExtra("Visit_image", retImage[0]);
                                    chatIntent.putExtra("Visit_user_name", retName );

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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout ,viewGroup,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;


            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();


    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder{


        TextView username, userstatus;
        CircleImageView profileimage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            username =  (TextView) itemView.findViewById(R.id.user_profile_name);
            userstatus = itemView.findViewById(R.id.user_status);
            profileimage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = (ImageView) itemView.findViewById(R.id.users_online_status);
        }
    }

     }





