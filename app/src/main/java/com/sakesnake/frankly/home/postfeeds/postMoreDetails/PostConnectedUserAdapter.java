package com.sakesnake.frankly.home.postfeeds.postMoreDetails;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.RandomColorGenerator;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

// Adapter for connected users
public class PostConnectedUserAdapter extends RecyclerView.Adapter<PostConnectedUserAdapter.PostConnectedUserViewHolder> {

    private Context mContext;

    private List<ParseUser> parseUserList;

    private PostFeedsCallback postFeedsCallback;

    public PostConnectedUserAdapter(Context mContext, List<ParseUser> parseUserList, PostFeedsCallback postFeedsCallback) {
        this.mContext = mContext;
        this.parseUserList = parseUserList;
        this.postFeedsCallback = postFeedsCallback;
    }

    public class PostConnectedUserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImageView,bottomLineImageView;
        TextView usernameTextView;
        public PostConnectedUserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.profile_pic_image_view);
            bottomLineImageView = (ImageView) itemView.findViewById(R.id.bottom_line_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.profile_username_text_view);

            itemView.setOnClickListener(view -> {
                if (postFeedsCallback != null)
                    postFeedsCallback.selectedPost(getAdapterPosition(),null,parseUserList.get(getAdapterPosition()));
            });
        }
    }

    @NonNull
    @Override
    public PostConnectedUserAdapter.PostConnectedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostConnectedUserViewHolder(LayoutInflater.from(mContext).inflate(R.layout.connected_users_list_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostConnectedUserAdapter.PostConnectedUserViewHolder holder, int position) {
        // Getting the parse user
        ParseUser parseUser = parseUserList.get(position);

        // Setting views with details
        ParseFile profilePic = parseUser.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null)
            Glide.with(mContext).load(profilePic.getUrl()).into(holder.profilePicImageView);

        holder.usernameTextView.setText(parseUser.getUsername());

        holder.bottomLineImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
    }

    @Override
    public int getItemCount() {
        return parseUserList.size();
    }
}
