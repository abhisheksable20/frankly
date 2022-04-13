package com.sakesnake.frankly.home.bottomnavupload.connectedusers;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sakesnake.frankly.R;
import com.parse.ParseUser;

import java.util.List;

public class ConnectedUsersAdapter extends ArrayAdapter<String>
{
    private Context mContext;
    private List<String> username;
    private List<ParseUser> parseUsers;
    private GetConnectedUser getConnectedUser;

    public interface GetConnectedUser
    {
        void getConnectedUser(ParseUser userObject);
    }

    public ConnectedUsersAdapter(@NonNull Context context, @NonNull List<String> objects,List<ParseUser> parseUsers,GetConnectedUser getConnectedUser)
    {
        super(context,0, objects);
        this.mContext = context;
        this.username = objects;
        this.getConnectedUser = getConnectedUser;
        this.parseUsers = parseUsers;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // init convert view
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.connected_users_item_list,parent,false);
        }

        // configuring string by changing color and adding ~ in textView
        String uName = "~ "+username.get(position);
        SpannableString spannableString = new SpannableString(uName);
        ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.upload_logo_color));
        spannableString.setSpan(span,0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Init views
        TextView connectedUserUsername = (TextView) convertView.findViewById(R.id.connected_users_username_text_view);

        // filling views with details
        connectedUserUsername.setText(spannableString);

        // getting username from on click method
        connectedUserUsername.setOnClickListener(v -> {
            if (getConnectedUser != null){
                getConnectedUser.getConnectedUser(parseUsers.get(position));
            }
        });

        return convertView;
    }

    @Override
    public int getCount()
    {
        return super.getCount();
    }
}
