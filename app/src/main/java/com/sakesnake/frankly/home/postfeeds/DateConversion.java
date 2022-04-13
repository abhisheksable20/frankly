package com.sakesnake.frankly.home.postfeeds;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConversion {
    public static String getPostTime(final long dateInMilliseconds){
        final Date date1 = new Date();
        final long currentTime = date1.getTime();
        final long elapsedMillisecond = currentTime - dateInMilliseconds;

        // Elapsed time is below 1 min
        if (elapsedMillisecond <= 60000){
            return "Just now";
        }

        // Elapsed time is below 60 min
        else if (elapsedMillisecond <= 3600000L){
            // Milliseconds in one minute
            final long oneMinMillisecond = 60000;
            final long elapsedTime = Math.round(Math.abs(elapsedMillisecond/oneMinMillisecond));
            if (elapsedTime == 1)
                return "1 minute ago";
            else
                return elapsedTime+" minutes ago";
        }

        // Elapsed time is below 24 hr
        else if (elapsedMillisecond <= 86400000L){
            // Milliseconds in one hr
            final long oneHrMillisecond = 3600000;
            final long elapsedTime = Math.round(Math.abs(elapsedMillisecond/oneHrMillisecond));
            if (elapsedTime == 1)
                return "1 hour ago";
            else
                return elapsedTime+" hours ago";
        }

        // Elapsed time is below 5 Days
        else if (elapsedMillisecond <= 432000000L){
            // Milliseconds in one day
            final long oneDayMillisecond = 86400000;
            final long elapsedTime = Math.round(Math.abs(elapsedMillisecond/oneDayMillisecond));
            if (elapsedTime == 1)
                return "1 day ago";
            else
                return elapsedTime+" days ago";
        }

        // Elapsed time is more than 5 Days showing directly an date
        else {
            date1.setTime(dateInMilliseconds);
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            return dateFormat.format(date1.getTime());
        }
    }
}
