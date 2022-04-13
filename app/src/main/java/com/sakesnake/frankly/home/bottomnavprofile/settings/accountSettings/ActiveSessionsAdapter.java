package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.postfeeds.DateConversion;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseSession;

import java.util.List;

// Adapter for account active session
public class ActiveSessionsAdapter extends RecyclerView.Adapter<ActiveSessionsAdapter.ActiveSessionsViewHolder> {

    private List<ParseSession> parseSessionList;

    private Context mContext;

    private ActiveSessionAdapterCallback activeSessionAdapterCallback;

    private ParseSession currentParseSession;

    public interface ActiveSessionAdapterCallback{
        void getSessionToDelete(ParseSession parseSession);
    }

    public ActiveSessionsAdapter(List<ParseSession> parseSessionList, Context mContext, ActiveSessionAdapterCallback activeSessionAdapterCallback, ParseSession currentSession) {
        this.parseSessionList = parseSessionList;
        this.mContext = mContext;
        this.activeSessionAdapterCallback = activeSessionAdapterCallback;
        this.currentParseSession = currentSession;
    }

    public class ActiveSessionsViewHolder extends RecyclerView.ViewHolder {
        CardView cardView,  deleteSessionCardView;
        TextView mobileNameTextView, loginTimeTextView;

        public ActiveSessionsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView3);
            deleteSessionCardView = (CardView) itemView.findViewById(R.id.delete_supporter_card_view);
            mobileNameTextView = (TextView) itemView.findViewById(R.id.supporter_username_text_view);
            loginTimeTextView = (TextView) itemView.findViewById(R.id.supporter_original_name_text_view);

            deleteSessionCardView.setOnClickListener(view -> {
                if (activeSessionAdapterCallback != null) {
                    int position = getAdapterPosition();
                    activeSessionAdapterCallback.getSessionToDelete(parseSessionList.get(position));
                    notifyItemRemoved(position);
                }
            });
        }
    }

    @NonNull
    @Override
    public ActiveSessionsAdapter.ActiveSessionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActiveSessionsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.supporters_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveSessionsAdapter.ActiveSessionsViewHolder holder, int position) {
        holder.cardView.setVisibility(View.GONE);
        ParseSession parseSession = parseSessionList.get(position);
        if (parseSession == null)
            return;

        if (currentParseSession != null && parseSession.getObjectId().equals(currentParseSession.getObjectId())) {
            holder.loginTimeTextView.setText(mContext.getString(R.string.current_session));
            holder.deleteSessionCardView.setVisibility(View.GONE);
        }
        else {
            holder.loginTimeTextView.setText(DateConversion.getPostTime(parseSession.getCreatedAt().getTime()));
            holder.deleteSessionCardView.setVisibility(View.VISIBLE);
        }

        String deviceName = parseSession.getString(ParseConstants.SESSION_MOBILE_NAME);
        if (deviceName != null)
            holder.mobileNameTextView.setText(deviceName);
        else
            holder.mobileNameTextView.setText(mContext.getString(R.string.unknown_device));
    }

    @Override
    public int getItemCount() {
        return parseSessionList.size();
    }
}
