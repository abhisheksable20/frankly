package com.sakesnake.frankly.home.bottomnavprofile.settings;

// Model class for account settings adapter
public class AccountSettings {

    private int settingType;

    private int settingIcon;

    private String settingName;

    public AccountSettings(int settingType, int settingIcon, String settingName) {
        this.settingType = settingType;
        this.settingIcon = settingIcon;
        this.settingName = settingName;
    }

    public int getSettingType() {
        return settingType;
    }

    public int getSettingIcon() {
        return settingIcon;
    }

    public String getSettingName() {
        return settingName;
    }
}

