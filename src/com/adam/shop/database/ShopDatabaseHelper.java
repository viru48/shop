package com.adam.shop.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.adam.shop.ChooseActivity;
import com.adam.shop.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShopDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todotable.db";
    private static final int DATABASE_VERSION = 9;
    private final Context context;


    public ShopDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        ChoiceTable.onCreate(database);
        ProductTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        ChoiceTable.onUpgrade(database, oldVersion, newVersion);
        ProductTable.onUpgrade(database, oldVersion, newVersion);
    }


    /**
     * Starts a thread to load the database table with words
     */
    public void loadFoods() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadWords();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void loadWords() throws IOException {
        Log.d(ChooseActivity.TAG, "Loading foods from text...");
        final Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.foods);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "~");
                if (strings.length < 2) continue;
                long foodID = Long.parseLong(strings[0]);
                long category = Long.parseLong(strings[2]);
                String name = strings[4].trim();
                String desc = strings[5].trim();
                String treatment = strings[6].trim();
                boolean wasAdded = addProduct(foodID, category, name, desc, treatment);
                if (!wasAdded) {
                    Log.e(ChooseActivity.TAG, "unable to add product: " + name);
                }
            }
        } finally {
            reader.close();
        }
        Log.d(ChooseActivity.TAG, "DONE loading products.");
    }

    /**
     * Add food to database.
     *
     * @return was added
     */
    public boolean addProduct(final long foodID, final long category, final String name, String desc, String treatment) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ProductTable.COLUMN_PRODUCT_ID, foodID);
        initialValues.put(ProductTable.COLUMN_CATEGORY, category);
        initialValues.put(ProductTable.COLUMN_NAME, name);
        initialValues.put(ProductTable.COLUMN_DESCRIPTION, desc);
        initialValues.put(ProductTable.COLUMN_TREATMENT, treatment);

        return getWritableDatabase().insert(ProductTable.TABLE_FTS, null, initialValues) != -1;
    }
}
