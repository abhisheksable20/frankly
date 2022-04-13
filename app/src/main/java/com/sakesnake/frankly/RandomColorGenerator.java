package com.sakesnake.frankly;

import android.content.Context;

import androidx.core.content.ContextCompat;

public class RandomColorGenerator {
    public static int generateRandomColor(Context mContext){
        int randomNumber = (int) Math.round(Math.random() * 7);
        switch (randomNumber){
            case 0:
                return ContextCompat.getColor(mContext,R.color.blue_thoughts_and_quotes);

            case 1:
                return ContextCompat.getColor(mContext,R.color.brown_give_them_answer);

            case 3:
                return ContextCompat.getColor(mContext,R.color.pink_poems);

            case 4:
                return ContextCompat.getColor(mContext,R.color.purple_top_profile);

            case 5:
                return ContextCompat.getColor(mContext,R.color.security_save_btn_color);

            case 6:
                return ContextCompat.getColor(mContext,R.color.warning_color);

            default:
                return ContextCompat.getColor(mContext,R.color.orange_memes_and_comedy);
        }
    }
}
