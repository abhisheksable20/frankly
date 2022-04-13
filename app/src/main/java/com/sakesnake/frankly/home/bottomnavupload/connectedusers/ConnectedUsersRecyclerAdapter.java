package com.sakesnake.frankly.home.bottomnavupload.connectedusers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseUser;

import java.util.List;

public class ConnectedUsersRecyclerAdapter extends RecyclerView.Adapter<ConnectedUsersRecyclerAdapter.ConnectedUsersViewHolder>
{
    private Context mContext;
    private List<ParseUser> parseUsersList;
    private RemoveConnectedUser removeConnectedUser;

    public interface RemoveConnectedUser
    {
        void removeConnectedUser(int position);
    }

    public ConnectedUsersRecyclerAdapter(Context mContext, List<ParseUser> parseUsersList, RemoveConnectedUser removeConnectedUser)
    {
        this.mContext = mContext;
        this.parseUsersList = parseUsersList;
        this.removeConnectedUser = removeConnectedUser;
    }

    public static class ConnectedUsersViewHolder extends RecyclerView.ViewHolder
    {
        ImageView connectedUsersProfilePhoto, removeConnectedUser;
        TextView connectedUsername, connectedName;

        public ConnectedUsersViewHolder(@NonNull View itemView)
        {
            super(itemView);
            connectedUsersProfilePhoto = (ImageView) itemView.findViewById(R.id.connected_users_recycler_profilephoto_image_view);
            removeConnectedUser = (ImageView) itemView.findViewById(R.id.connected_users_recycler_delete_user_image_view);
            connectedUsername = (TextView) itemView.findViewById(R.id.connected_users_recycler_username_text_view);
            connectedName = (TextView) itemView.findViewById(R.id.connected_users_recycler_name_text_view);
        }
    }

    @NonNull
    @Override
    public ConnectedUsersRecyclerAdapter.ConnectedUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.connected_users_recycler_list_view,parent,false);
        return new ConnectedUsersViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedUsersRecyclerAdapter.ConnectedUsersViewHolder holder, int position)
    {
        ParseUser user = parseUsersList.get(position);

        holder.connectedUsername.setText(user.getUsername());
        holder.connectedName.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));

        holder.removeConnectedUser.setOnClickListener(v ->
        {
            notifyItemRemoved(position);
            removeConnectedUser.removeConnectedUser(position);
        });
    }

    @Override
    public int getItemCount()
    {
        return parseUsersList.size();
    }
}
