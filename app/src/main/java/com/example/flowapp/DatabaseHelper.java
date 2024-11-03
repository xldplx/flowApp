package com.example.flowapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flowapp5.db";
    private static final int DATABASE_VERSION = 2;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_PIN = "user_pin";
    public static final String COLUMN_USER_MONEY = "user_money";

    // Transactions table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_USER_ID_FK = "user_id"; // Foreign key to users table
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_PIN + " TEXT NOT NULL, " +
                    COLUMN_USER_MONEY + " REAL DEFAULT 0);";

    private static final String TABLE_CREATE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK + " INTEGER, " +
                    COLUMN_AMOUNT + " REAL, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_TRANSACTION_TYPE + " TEXT, " +
                    "FOREIGN KEY (" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // If you want to add a new column, you can use ALTER TABLE
            // Otherwise, you might want to drop the table and recreate it
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN balance REAL DEFAULT 0");
        }
    }

    public boolean updateUserBalance(int userId, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_MONEY, newBalance); // Use COLUMN_USER_MONEY instead of "balance"

        // Update the user balance in the database
        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();

        return rowsAffected > 0; // Return true if at least one row was updated
    }

    public boolean addTransaction(int userId, double amount, String date, String title, String transactionType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_TRANSACTION_TYPE, transactionType);
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return false;
    }

    public void addUser (String pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PIN, pin);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public boolean checkUser (String pin) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_PIN + "=?", new String[]{pin}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public double getUserBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_MONEY}, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        double balance = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(cursor.getColumnIndex(COLUMN_USER_MONEY));
            }
            cursor.close();
        }
        return balance;
    }

    public int getUserIdByPin(String pin) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID}, COLUMN_USER_PIN + "=?", new String[]{pin}, null, null, null);
        int userId = -1; // Default value if not found
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            }
            cursor.close();
        }
        return userId;
    }

    public List<Transaction> getUserTransactions(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch transactions for the given user ID
        Cursor cursor = db.query(TABLE_TRANSACTIONS,
                new String[]{COLUMN_TRANSACTION_ID, COLUMN_AMOUNT, COLUMN_DATE, COLUMN_TITLE, COLUMN_TRANSACTION_TYPE},
                COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_DATE + " DESC"); // Order by date descending

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int transactionId = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));

                // You can use different images based on transaction type if needed
                int imageResourceId = R.drawable.pay_bills; // Default image for all transactions
                String transactionType = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSACTION_TYPE));
                if ("payment".equals(transactionType)) {
                    imageResourceId = R.drawable.pay_bills; // Change to the appropriate image
                } else if ("receive".equals(transactionType)) {
                    imageResourceId = R.drawable.receive; // Change to the appropriate image
                }
                // Create a Transaction object and add it to the list
                Transaction transaction = new Transaction(transactionId, amount, date, title, imageResourceId);
                transactions.add(transaction);
            }
            cursor.close();
        }
        return transactions;
    }
}