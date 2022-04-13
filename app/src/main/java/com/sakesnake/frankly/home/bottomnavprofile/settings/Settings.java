package com.sakesnake.frankly.home.bottomnavprofile.settings;

public class Settings
{
    private int settingsLogo;
    private String settingsName;

    public Settings(int settingsLogo, String settingsName)
    {
        this.settingsLogo = settingsLogo;
        this.settingsName = settingsName;
    }

    public int getSettingsLogo()
    {
        return settingsLogo;
    }

    public String getSettingsName()
    {
        return settingsName;
    }
}
