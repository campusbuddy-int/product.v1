package com.ng.campusbuddy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ng.campusbuddy.Fragments.ProfileFragment;
import com.ng.campusbuddy.MainActivity;
import com.ng.campusbuddy.Message.ChatActivity;
import com.ng.campusbuddy.Model.Chat;
import com.ng.campusbuddy.Model.User;
import com.ng.campusbuddy.R;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;
    private boolean isFragment;

        String theLastMessage;

    public ChatUserAdapter(Context mContext, List<User> mUsers, boolean ischat, boolean isFragment){

        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.isFragment = isFragment;
        }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_user_item, parent, false);
        return new ChatUserAdapter.ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageurl().equals("")){
        holder.profile_image.setImageResource(R.drawable.profile);
        } else {
        Glide.with(mContext).load(user.getImageurl()).into(holder.profile_image);
        }

        if (ischat){
        lastMessage(user.getId(), holder.last_msg);
        } else {
        holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
        if (user.getStatus().equals("online")){
        holder.img_on.setVisibility(View.VISIBLE);
        holder.img_off.setVisibility(View.GONE);
        } else {
        holder.img_on.setVisibility(View.GONE);
        holder.img_off.setVisibility(View.VISIBLE);
        }
        } else {
        holder.img_on.setVisibility(View.GONE);
        holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra("userid", user.getId());
        mContext.startActivity(intent);
        }
        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getId());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });
        }

    @Override
    public int getItemCount() {
        return mUsers.size();
        }

    public  class ViewHolder extends RecyclerView.ViewHolder{

    public TextView username;
    public ImageView profile_image;
    private ImageView img_on;
    private ImageView img_off;
    private TextView last_msg;

    public ViewHolder(View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        profile_image = itemView.findViewById(R.id.profile_image);
        img_on = itemView.findViewById(R.id.img_on);
        img_off = itemView.findViewById(R.id.img_off);
        last_msg = itemView.findViewById(R.id.last_msg);
    }
}

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                switch (theLastMessage){
                    case  "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
