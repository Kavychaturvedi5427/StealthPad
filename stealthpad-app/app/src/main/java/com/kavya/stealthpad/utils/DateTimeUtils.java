package com.kavya.stealthpad.utils;

import android.text.format.DateUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    /**
     * Formats a timestamp into a human-friendly relative string (e.g., "5 min ago", "Yesterday")
     * for recent dates, or an absolute string for older dates.
     */
    public static String formatRelativeTime(long timestamp, String prefix) {
        if (timestamp <= 0) {
            return prefix + " recently";
        }

        long millis = (timestamp < 1000000000000L) ? timestamp * 1000 : timestamp;
        long now = System.currentTimeMillis();

        // Handle timestamps slightly in the future (e.g., due to server clock drift)
        if (millis > now && (millis - now) < DateUtils.MINUTE_IN_MILLIS) {
            return prefix + " just now";
        }

        // Use Android's DateUtils for relative time
        if (now - millis < DateUtils.WEEK_IN_MILLIS && now - millis >= 0) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    millis, 
                    now, 
                    DateUtils.MINUTE_IN_MILLIS, 
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            return prefix + " " + relativeTime;
        }

        // For older dates, use absolute format: "14 Sep, 8:42 PM"
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault());
        return prefix + " " + formatter.format(new Date(millis));
    }

    /**
     * Formats a timestamp into a standard absolute format.
     */
    public static String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return "---";
        }

        long millis = (timestamp < 1000000000000L) ? timestamp * 1000 : timestamp;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return formatter.format(new Date(millis));
    }
}
