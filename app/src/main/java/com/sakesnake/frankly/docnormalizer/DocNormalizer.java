package com.sakesnake.frankly.docnormalizer;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class DocNormalizer {

    // Converting html string to spanned
    public static CharSequence htmlToNormal(final String htmlString){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Spanned spanned = Html.fromHtml(htmlString,Html.FROM_HTML_MODE_COMPACT);
            String str = spanned.toString().trim();
            return spanned.subSequence(0,str.length());
        }
        else{
            Spanned spanned = Html.fromHtml(htmlString);
            String str = spanned.toString().trim();
            return spanned.subSequence(0,str.length());
        }
    }

    // Converting spanned text to html string
    public static String spannedToHtml(final Spanned spanned){
        String htmlString;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            htmlString = Html.toHtml(spanned,Html.FROM_HTML_MODE_LEGACY);
        }
        else{
            htmlString = Html.toHtml(spanned);
        }
        return htmlString+"";
    }
}
