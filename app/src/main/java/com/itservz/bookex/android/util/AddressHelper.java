package com.itservz.bookex.android.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Raju on 4/12/2017.
 */

public class AddressHelper {
    static final String TAG = "AddressHelper";

    public static String getAddress(Context context, Location location) {
        Log.d(TAG, "" + location);
        if (location == null) {
            return " ";
        }
        String add = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return " ";
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            illegalArgumentException.printStackTrace();
            return " ";
        }

        if (addresses == null || addresses.size() == 0) {
            Log.d(TAG, "no address");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            add = addressFragments.toString();
            Log.d(TAG, "add.toString() " + add);
            Log.d(TAG, "address.toString() " + address.toString());
        }
        return add;
    }

    public static String getAddress(Context context, com.itservz.bookex.android.model.Location location) {
        Log.d(TAG, "" + location);
        if (location == null) {
            return " ";
        }
        String add = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return " ";
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            illegalArgumentException.printStackTrace();
            return " ";
        }

        if (addresses == null || addresses.size() == 0) {
            Log.d(TAG, "no address");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            add = addressFragments.toString();
            Log.d(TAG, "add.toString() " + add);
            Log.d(TAG, "address.toString() " + address.toString());
        }
        return add;
    }
}
