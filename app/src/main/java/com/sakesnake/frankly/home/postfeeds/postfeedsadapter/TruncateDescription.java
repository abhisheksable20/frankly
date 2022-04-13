package com.sakesnake.frankly.home.postfeeds.postfeedsadapter;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class TruncateDescription {
    public static Spanned getTruncatedDescription(String htmlString, int color){
        SpannableString spannableString;
        int indexBr = htmlString.indexOf("<br>");
        if (indexBr != -1  &&  indexBr < 40){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spannableString = new SpannableString(Html.fromHtml(htmlString.substring(0,indexBr)+"...more</p>",Html.FROM_HTML_MODE_LEGACY));
            else
                spannableString = new SpannableString(Html.fromHtml(htmlString.substring(0,indexBr)+"...more</p>"));

            spannableString.setSpan(new ForegroundColorSpan(color),spannableString.length()-7,spannableString.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }else{
            String desc;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                desc = Html.fromHtml(htmlString,Html.FROM_HTML_MODE_LEGACY).toString().trim();
            else
                desc = Html.fromHtml(htmlString).toString().trim();

            if (desc.length() > 40) {
                spannableString = new SpannableString(desc+"...more");
                spannableString.setSpan(new ForegroundColorSpan(color),spannableString.length()-7,spannableString.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            }
            else
                return spannableString = new SpannableString(desc);

        }
    }
}
