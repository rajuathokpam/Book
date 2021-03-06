package com.itservz.bookex.android.util;

import android.util.Log;

import com.itservz.bookex.android.model.Location;

/**
 * Created by Raju on 12/28/2016.
 */

public class DistanceCalculator {

    private static final String TAG = "DistanceCalculator";

    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        Log.d(TAG, "cordinates: " + lat1 + " " + lng1 + " " + lat2 + " " + lng2);
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        Log.d(TAG, "distance: " + dist);
        return dist;
    }
}
