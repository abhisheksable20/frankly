package com.sakesnake.frankly.home.bottomnavprofile.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;

import java.util.List;

// Adapter for account settings
public class AccountSettingsAdapter extends RecyclerView.Adapter<AccountSettingsAdapter.AccountSettingsViewHolder> {

    private Context mContext;

    private List<AccountSettings> accountSettingsList;

    private SettingsAdapterCallback settingsAdapterCallback;

    public AccountSettingsAdapter(Context mContext, List<AccountSettings> accountSettingsList, SettingsAdapterCallback settingsAdapterCallback) {
        this.mContext = mContext;
        this.accountSettingsList = accountSettingsList;
        this.settingsAdapterCallback = settingsAdapterCallback;
    }

    public class AccountSettingsViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView settingIcon;
        TextView settingName;
        public AccountSettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            settingIcon = (ImageView) itemView.findViewById(R.id.setting_icon_image_view);
            settingName = (TextView) itemView.findViewById(R.id.setting_name_text_view);

            itemView.setOnTouchListener(this);

            itemView.setOnClickListener(view -> {
                if (settingsAdapterCallback != null)
                    settingsAdapterCallback.getSettingType(accountSettingsList.get(getAdapterPosition()).getSettingType());
            });
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();
            switch (event){
                case MotionEvent.ACTION_DOWN: {
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:{
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
                }
            }
            return false;
        }
    }

    @NonNull
    @Override
    public AccountSettingsAdapter.AccountSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountSettingsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.account_settings_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountSettingsAdapter.AccountSettingsViewHolder holder, int position) {
        AccountSettings accountSettings = accountSettingsList.get(position);
        holder.settingIcon.setImageResource(accountSettings.getSettingIcon());
        holder.settingName.setText(accountSettings.getSettingName());
    }

    @Override
    public int getItemCount() {
        return accountSettingsList.size();
    }
}
