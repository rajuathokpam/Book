package com.itservz.bookex.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.itservz.bookex.android.backend.FirebaseCategoryService;
import com.itservz.bookex.android.backend.FirebaseDatabaseService;
import com.itservz.bookex.android.backend.FirebaseStorageService;
import com.itservz.bookex.android.backend.ImagePickerService;
import com.itservz.bookex.android.databinding.ActivitySellBinding;
import com.itservz.bookex.android.model.Book;
import com.itservz.bookex.android.model.BookCategory;
import com.itservz.bookex.android.model.SellHandler;
import com.itservz.bookex.android.preference.PrefManager;
import com.itservz.bookex.android.util.BitmapHelper;
import com.itservz.bookex.android.util.ScreenSizeScaler;
import com.itservz.bookex.android.view.FlowLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class SellActivity extends BaseActivity implements
        View.OnClickListener, FirebaseCategoryService.CategoryListener {
    protected static final String TAG = "SellActivity";
    private Book book;
    private List<BookCategory> categories = new ArrayList<>();
    int PICK_IMAGE = 1;
    private ImageView bookImage;
    private InputStream imageStream;
    PrefManager prefManager = null;
    private final static int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;

    protected TextView mLocationAddressTextView;

    private FlowLayout bookCategoriesLayout;
    private int SCANNER_REQUEST_CODE = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkLogin();
        //setContentView(R.layout.activity_sell);

        book = new Book();
        //http://www.singhajit.com/android-data-binding/
        ActivitySellBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_sell);
        binding.setBook(book);
        binding.setHandler(new SellHandler(this.getApplicationContext()));

        new FirebaseCategoryService(this).getCategories();
        bookCategoriesLayout = (FlowLayout) findViewById(R.id.book_categories);

        prefManager = new PrefManager(this);

        bookImage = (ImageView) findViewById(R.id.book_image);
        bookImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooserIntent = new ImagePickerService().getImageFromPhoneIntent();
                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        //scanner
        findViewById(R.id.scanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(SellActivity.this, SimpleScannerActivity.class),
                        SCANNER_REQUEST_CODE);
            }
        });

        findViewById(R.id.sell_button).setOnClickListener(this);

        mLatitudeLabel = "Latitude";
        mLongitudeLabel = "Longitude";

        updateUIWidgets();
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onCategoryAdded(final BookCategory bookCategory) {

        LinearLayout categoryLL = new LinearLayout(this);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryLL.setLayoutParams(layout);
        ScreenSizeScaler screenSizeScaler = new ScreenSizeScaler(getResources());
        final int px = screenSizeScaler.getdpAspixel(8);
        categoryLL.setPadding(px, px, px, px);

        final TextView textView = new TextView(this);
        textView.setLayoutParams(layout);
        textView.setBackground(getResources().getDrawable(R.drawable.rounded_border));
        textView.setPadding(px, px, px, px);
        textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        textView.setText(bookCategory.longText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = textView.getText().toString();
                if ("added".equals(textView.getTag())) {//deselect
                    textView.setBackground(getResources().getDrawable(R.drawable.rounded_border));
                    textView.setPadding(px, px, px, px);
                    if (categories.contains(bookCategory)) {
                        categories.remove(bookCategory);
                    }
                    textView.setTag("");
                } else {
                    textView.setBackground(getResources().getDrawable(R.drawable.rounded_border_selected));
                    textView.setPadding(px, px, px, px);
                    if (!categories.contains(bookCategory)) {
                        categories.add(bookCategory);
                    }
                    textView.setTag("added");
                }
            }
        });

        categoryLL.addView(textView);
        bookCategoriesLayout.addView(categoryLL, 0);
    }

    @Override
    public void onClick(View v) {
        TextInputLayout title = (TextInputLayout) findViewById(R.id.title_wrapper);
        if(book.getTitle() == null){
            title.setError("Please enter the title");
            return;
        } else{
            title.setError(null);
        }
        TextInputLayout authorWrapper = (TextInputLayout) findViewById(R.id.author_wrapper);
        if(book.getAuthor() == null){
            authorWrapper.setError("Please enter the author");
            return;
        } else {
            authorWrapper.setError(null);
        }
        TextInputLayout conditionWrapper = (TextInputLayout) findViewById(R.id.condition_wrapper);
        if(book.getCondition() == null){
            conditionWrapper.setError("Please enter a short description");
            return;
        } else {
            conditionWrapper.setError(null);
        }
        TextInputLayout priceWrapper = (TextInputLayout) findViewById(R.id.yourprice_wrapper);
        if(book.getYourPrice() == 0){
            priceWrapper.setError("Enter for how much do you want to sell!");
            return;
        } else {
            priceWrapper.setError(null);
        }
        TextInputLayout phoneWrapper = (TextInputLayout) findViewById(R.id.phone_wrapper);
        if(book.getPhoneNumber() == null || !isValidPhoneNo(book.getPhoneNumber())){
            phoneWrapper.setError("Enter a valid mobile number");
            return;
        } else {
            phoneWrapper.setError(null);
        }
        boolean hasCat = false;
        LinearLayout categoryLL = (LinearLayout) bookCategoriesLayout.getChildAt(0);
        for(int index = 0 ; index < categoryLL.getChildCount(); index ++){
            Object tag = categoryLL.getChildAt(index).getTag();
            String s =  tag == null ? " null " : tag.toString();
            Log.d(TAG, "onClickTag: " +s );

            if(tag != null && "added".equals(tag.toString())){
                hasCat = true;
                break;
            }
        }
        if(!hasCat){
            Toast.makeText(this, "Please choose one or more categories.", Toast.LENGTH_LONG).show();
            return;
        }


        book.setISBN(prefManager.getISBN());
        //book.setTitle(prefManager.getTitle());
        book.setUploadTime(-1 * new Date().getTime());
        book.setCategories(categories);
        book.seller.name = getLoginDisplayName();
        book.seller.email = getLoginEmail();
        String uId = FirebaseDatabaseService.getInstance("").addSellingItem(book);
        Bitmap bitmap = null;
        if(bookImage.getDrawable() == null){
            Resources res = getResources();
            Drawable drawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawable = res.getDrawable(R.drawable.no_book, null);
            } else{
                drawable = res.getDrawable(R.drawable.no_book);
            }
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        } else {
            bookImage.setDrawingCacheEnabled(true);
            bookImage.buildDrawingCache();
            bitmap = bookImage.getDrawingCache();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //80 is recommended - http://stackoverflow.com/questions/35271817/compressing-image-in-android-is-loosing-the-quality-when-image-is-taken-from-pho
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        FirebaseStorageService.getInstance().setImage(uId, baos.toByteArray(), this);

        Snackbar.make(v, "Ad posted", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        finish();
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    public void fetchAddressButtonHandler(View view) {
        // We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, we process the user's request by setting
        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateUIWidgets();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            Log.d(TAG, imageUri.getPath());
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                Log.d(TAG, "imageStream "+imageStream.markSupported());
                //Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromInputStream(getResources(), imageStream);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                Log.d(TAG, "About to set to image view " + bitmap.getByteCount());
                bookImage.setImageBitmap(bitmap);
                bookImage.setBackground(null);
            } catch (FileNotFoundException e) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                    }
                }
            }
        } else if(requestCode == SCANNER_REQUEST_CODE){
            String isbn = data.getStringExtra("ISBN");
            Log.d(TAG, "onActivityResult: isbn" + isbn);
            book.setISBN(isbn);
            ((TextInputEditText)findViewById(R.id.isbn)).setText(isbn);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromInputStream(getResources(), imageStream);
                    bookImage.setImageBitmap(bitmap);
                    bookImage.setBackground(null);
                } else {
                    Toast.makeText(this, "Denied", Toast.LENGTH_LONG);
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        if (mLastLocation != null) {
            /*mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                    mLastLocation.getLatitude()));
            mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                    mLastLocation.getLongitude()));*/

            book.getLocation().latitude = mLastLocation.getLatitude();
            book.getLocation().longitude = mLastLocation.getLongitude();

            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
        } else {
            Toast.makeText(this, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the address in the UI.
     */
    void displayAddressOutput() {
        mLocationAddressTextView.setText(mAddressOutput);
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    void updateUIWidgets() {
        /*if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mFetchAddressButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mFetchAddressButton.setEnabled(true);
        }*/
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }

    private boolean isValidPhoneNo(CharSequence phoneNo) {
        String pattern = "^(?:(?:\\+|0{0,2})91(\\s*[\\ -]\\s*)?|[0]?)?[789]\\d{9}|(\\d[ -]?){10}\\d$";
        return Pattern.matches(pattern, phoneNo);
    }

}