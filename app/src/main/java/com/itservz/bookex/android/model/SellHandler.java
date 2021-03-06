package com.itservz.bookex.android.model;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.itservz.bookex.android.backend.GoogleBooksAPIService;

/**
 * Created by Raju on 2/5/2017.
 */

public class SellHandler {
    private static final String TAG = "SellHandler";
    private final Context context;

    public SellHandler(Context context){
        this.context = context;
    }

    public View.OnClickListener click(final Book book) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String isbnText = book.getISBN();
                Log.d(TAG, "onClick:ISBN " + isbnText);
                new GoogleBooksAPIService().getBook(isbnText, book, context);
            }
        };
    }
}
