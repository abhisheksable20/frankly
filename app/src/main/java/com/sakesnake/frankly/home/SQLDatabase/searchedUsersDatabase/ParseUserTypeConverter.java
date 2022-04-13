package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseUser;

import java.lang.reflect.Type;

public class ParseUserTypeConverter {
    @TypeConverter
    public static ParseUser jsonToParseUser(String jsonString){
        if (jsonString == null)
            return null;


        Type objectType = new TypeToken<ParseUser>(){}.getType();
        return new Gson().fromJson(jsonString,objectType);
    }

    @TypeConverter
    public static String objectToJson(ParseUser user){
        return new Gson().toJson(user);
    }
}
