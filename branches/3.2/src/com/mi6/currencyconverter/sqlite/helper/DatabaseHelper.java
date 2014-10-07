package com.mi6.currencyconverter.sqlite.helper;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mi6.currencyconverter.sqlite.model.CurrencyDetails;
import com.mi6.currencyconverter.sqlite.model.RateDetails;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	SQLiteDatabase db;
	Context context;

	// Logcat tag
    private static final String LOG = "DatabaseHelper";
 
    // Database Version
    private static final int DATABASE_VERSION = 4;
 
    // Database Name
    private static final String DATABASE_NAME = "currency_converter";
 
    // Table Names
    private static final String TABLE_CURRENCY_DETAILS = "currency_details";
    private static final String TABLE_RATE_DETAILS = "rate_details";
	
 // CURRENCY_DETAILS Table - column names
    private static final String CD_CODE = "code";
	private static final String CD_NAME = "name";
	private static final String CD_COUNTRY = "country";
	private static final String CD_FLAG = "flag";
 
    // RATE_DETAILS Table - column names
	private static final String RD_SOURCE_CURRENCY_CODE = "sourceCurrencyCode";
	private static final String RD_TARGET_CURRENCY_CODE = "targetCurrencyCode";
	private static final String RD_RATE_DATE = "rateDate";
	private static final String RD_RATE_VALUE = "rateValue";
	
	// Table Create Statements
    // currency_details table create statement
    private static final String CREATE_TABLE_CD = "CREATE TABLE "
            + TABLE_CURRENCY_DETAILS + "(" + CD_CODE + " TEXT PRIMARY KEY," + CD_NAME
            + " TEXT," + CD_COUNTRY + " TEXT," + CD_FLAG
            + " TEXT" + ")";
 
    // rate_details table create statement
    private static final String CREATE_TABLE_RD = "CREATE TABLE " + TABLE_RATE_DETAILS
            + "(" + RD_SOURCE_CURRENCY_CODE + " TEXT NOT NULL," + RD_TARGET_CURRENCY_CODE + " TEXT NOT NULL,"
            + RD_RATE_DATE + " DATETIME NOT NULL," + RD_RATE_VALUE + " REAL" 
            +", PRIMARY KEY (" + RD_SOURCE_CURRENCY_CODE + "," + RD_TARGET_CURRENCY_CODE + "," +RD_RATE_DATE+ "))";
 
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        //db.execSQL(CREATE_TABLE_CD);
        Log.i("CREATE_TABLE_RD", CREATE_TABLE_RD);
        db.execSQL(CREATE_TABLE_RD);
       /* 
     // Create Parser for raw/currencies.xml
     	CurrencyParser currencyParser = new CurrencyParser();
     	InputStream inputStream = context.getResources().openRawResource(R.raw.currencies);
     				
     	// Parse the inputstream
     	currencyParser.parse(inputStream);

     	// Get Currencies
     	List<CurrencyDetails> currencyList = currencyParser.getListForSQLite();
     	
     	for(CurrencyDetails cd:currencyList) {
     		addCurrencyDetails(cd);
     	}
     	*/
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATE_DETAILS);
 
        // create new tables
        onCreate(db);
    }
    
    /*
     * inserting a currency
     */
    public long addCurrencyDetails(CurrencyDetails cd) {
        db = this.getWritableDatabase();
        long rowId = -1;
        ContentValues values = new ContentValues();
        values.put(CD_CODE, cd.getCode());
        values.put(CD_NAME, cd.getName());
        values.put(CD_COUNTRY, cd.getCountry());
        values.put(CD_COUNTRY, cd.getFlag());
     
        // insert row
        try {
        	rowId = db.insert(TABLE_CURRENCY_DETAILS, null, values);
        } catch (SQLiteConstraintException e) {
			Log.e(TABLE_RATE_DETAILS, e.toString());
		}
     
        return rowId;
    }
    
    /*
     * get currencyDetails(one) by code
     */
    public CurrencyDetails getCurrencyDetailsByCode(String code) {
        db = this.getReadableDatabase();
     
        String selectQuery = "SELECT  * FROM " + TABLE_CURRENCY_DETAILS + " WHERE "
                + CD_CODE + " = '" + code +"'";
     
        Log.e(LOG, selectQuery);
     
        Cursor c = db.rawQuery(selectQuery, null);
     
        if (c != null)
            c.moveToFirst();
     
        CurrencyDetails cd = new CurrencyDetails();
        cd.setCode(c.getString(c.getColumnIndex(CD_CODE)));
        cd.setName((c.getString(c.getColumnIndex(CD_NAME))));
        cd.setCountry((c.getString(c.getColumnIndex(CD_COUNTRY))));
        cd.setFlag((c.getString(c.getColumnIndex(CD_FLAG))));
     
        return cd;
    }
    
    /*
     * getting all currencies
     * */
    public List<CurrencyDetails> getAllToCurrencies() {
        List<CurrencyDetails> currencies = new ArrayList<CurrencyDetails>();
        String selectQuery = "SELECT  * FROM " + TABLE_CURRENCY_DETAILS;
     
        Log.e(LOG, selectQuery);
     
        db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                CurrencyDetails cd = new CurrencyDetails();
                cd.setCode(c.getString(c.getColumnIndex(CD_CODE)));
                cd.setName((c.getString(c.getColumnIndex(CD_NAME))));
                cd.setCountry((c.getString(c.getColumnIndex(CD_COUNTRY))));
                cd.setFlag((c.getString(c.getColumnIndex(CD_FLAG))));
     
                // adding to currencies list
                currencies.add(cd);
            } while (c.moveToNext());
        }
     
        return currencies;
    }
    
    /*
     * Updating a CurrencyDetailsDto
     */
    public int updateCurrencyDetails(CurrencyDetails cd) {
        db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(CD_NAME, cd.getName());
        values.put(CD_COUNTRY, cd.getCountry());
        values.put(CD_FLAG, cd.getFlag());
     
        // updating row
        return db.update(TABLE_CURRENCY_DETAILS, values, CD_CODE + " = ?",
                new String[] { String.valueOf(cd.getCode()) });
    }
    
    /*
     * Deleting a CurrencyDetailsDto
     */
    public void deleteCurrencyDetails(String code) {
        db = this.getWritableDatabase();
        db.delete(TABLE_CURRENCY_DETAILS, CD_CODE + " = ?",
                new String[] { String.valueOf(code) });
    }
    
    /*
     * inserting a rate
     */
    public long addRateDetails(RateDetails rd) {
        db = this.getWritableDatabase();
        long rowId = -1;
        ContentValues values = new ContentValues();
        values.put(RD_SOURCE_CURRENCY_CODE, rd.getSourceCurrencyCode());
        values.put(RD_TARGET_CURRENCY_CODE, rd.getTargetCurrencyCode());
        values.put(RD_RATE_DATE, rd.getRateDate());
        values.put(RD_RATE_VALUE, rd.getRateValue());
     
        // insert row
        try {
        	rowId = db.insertOrThrow(TABLE_RATE_DETAILS, null, values);
        } catch (SQLiteConstraintException e) {
			Log.e(TABLE_RATE_DETAILS, e.toString());
		}
     
        return rowId;
    }
    
    /*
     * inserting multiple rates
     */
    public List<Long> addMultipleRateDetails(List<RateDetails> rdList) {
        db = this.getWritableDatabase();
        long rowId = -1;
        List<Long> rowIds = new ArrayList<Long>();
        db.beginTransaction();
        for (RateDetails rd:rdList) {
			ContentValues values = new ContentValues();
			values.put(RD_SOURCE_CURRENCY_CODE, rd.getSourceCurrencyCode());
			values.put(RD_TARGET_CURRENCY_CODE, rd.getTargetCurrencyCode());
			values.put(RD_RATE_DATE, rd.getRateDate());
			values.put(RD_RATE_VALUE, rd.getRateValue());
			
			// insert row
			try {
				
				rowId = db.insertOrThrow(TABLE_RATE_DETAILS, null, values);
			} catch (SQLiteConstraintException e) {
				Log.e(TABLE_RATE_DETAILS, e.toString());
			}
			rowIds.add(rowId);
		}
        db.setTransactionSuccessful();
        db.endTransaction();
        
     
        return rowIds;
    }
    
    /*
     * get rate by sourceCurrency, targetCurrency and date 
     */
    public Double getRateForSpecificDay(String sourceCurrency, String targetCurrency, Date date) {
        db = this.getReadableDatabase();
        Double rate = null;
     
        String selectQuery = "SELECT  * FROM " + TABLE_RATE_DETAILS + " WHERE "
                + RD_SOURCE_CURRENCY_CODE + " = '" + sourceCurrency + "' AND " + RD_TARGET_CURRENCY_CODE + " = '" + targetCurrency + "' AND "
                + RD_RATE_DATE + " = '" + date.toString() + "'";
     
        Log.e(LOG, selectQuery);
     
        Cursor c = db.rawQuery(selectQuery, null);
     
        if (c != null){
            c.moveToFirst();
            rate = c.getDouble(c.getColumnIndex(RD_RATE_VALUE));
        }
     
        return rate;
    }
    
    /*
     * get all rates by sourceCurrency and date 
     */
    public List<RateDetails> getAllRatesForSpecificDay(String sourceCurrency, Date date) {
        db = this.getReadableDatabase();
        List<RateDetails> rates = new ArrayList<RateDetails>();;
     
        String selectQuery = "SELECT  * FROM " + TABLE_RATE_DETAILS + " WHERE "
                + RD_SOURCE_CURRENCY_CODE + " = '" + sourceCurrency + "' AND " + RD_RATE_DATE + " = '" + date.toString() + "'";
     
        Log.e(LOG, selectQuery);
     
        Cursor c = db.rawQuery(selectQuery, null);
     
     // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	RateDetails rd = new RateDetails();
                rd.setSourceCurrencyCode(c.getString(c.getColumnIndex(RD_SOURCE_CURRENCY_CODE)));
                rd.setTargetCurrencyCode(c.getString(c.getColumnIndex(RD_TARGET_CURRENCY_CODE)));
                rd.setRateDate(c.getString(c.getColumnIndex(RD_RATE_DATE)));
                rd.setRateValue(c.getDouble(c.getColumnIndex(RD_RATE_VALUE)));
                // adding to currencies list
                rates.add(rd);
            } while (c.moveToNext());
        }
     
        return rates;
    }
    
    /*
     * get historical data between sourceCurrency and targetCurrency
     */
    public List<RateDetails> getHistoricalDataForRate(String sourceCurrency, String targetCurrency) {
        db = this.getReadableDatabase();
        List<RateDetails> rates = new ArrayList<RateDetails>();;
     
        String selectQuery = "SELECT  * FROM " + TABLE_RATE_DETAILS + " WHERE "
                + RD_SOURCE_CURRENCY_CODE + " = '" + sourceCurrency + "' AND " + RD_TARGET_CURRENCY_CODE + " = '" + targetCurrency + "'";
     
        Log.e(LOG, selectQuery);
     
        Cursor c = db.rawQuery(selectQuery, null);
     
     // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	RateDetails rd = new RateDetails();
                rd.setSourceCurrencyCode(c.getString(c.getColumnIndex(RD_SOURCE_CURRENCY_CODE)));
                rd.setTargetCurrencyCode(c.getString(c.getColumnIndex(RD_TARGET_CURRENCY_CODE)));
                rd.setRateDate(c.getString(c.getColumnIndex(RD_RATE_DATE)));
                rd.setRateValue(c.getDouble(c.getColumnIndex(RD_RATE_VALUE)));
                // adding to currencies list
                rates.add(rd);
            } while (c.moveToNext());
        }
     
        return rates;
    }
    
    /*
     * getting all rates for a specific currency
     * */
    public List<RateDetails> getRatesForSpecificCurrency(String sourceCurrency) {
        List<RateDetails> rates = new ArrayList<RateDetails>();
        String selectQuery = "SELECT  * FROM " + TABLE_RATE_DETAILS + " WHERE " + RD_SOURCE_CURRENCY_CODE + " = " + sourceCurrency;
     
        Log.e(LOG, selectQuery);
     
        db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	RateDetails rd = new RateDetails();
                rd.setSourceCurrencyCode(c.getString(c.getColumnIndex(RD_SOURCE_CURRENCY_CODE)));
                rd.setTargetCurrencyCode(c.getString(c.getColumnIndex(RD_TARGET_CURRENCY_CODE)));
                rd.setRateDate(c.getString(c.getColumnIndex(RD_RATE_DATE)));
                rd.setRateValue(c.getDouble(c.getColumnIndex(RD_RATE_VALUE)));
                // adding to currencies list
                rates.add(rd);
            } while (c.moveToNext());
        }
     
        return rates;
    }
    
    /*
     * Updating a RateDetails
     */
    public int updateRateDetails(RateDetails rd) {
        db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(RD_RATE_VALUE, rd.getRateValue());
     
        // updating row
        return db.update(TABLE_RATE_DETAILS, values, RD_SOURCE_CURRENCY_CODE + " = ?," + RD_TARGET_CURRENCY_CODE + " = ?," + RD_RATE_DATE + " = ?",
                new String[] { String.valueOf(rd.getSourceCurrencyCode()), String.valueOf(rd.getTargetCurrencyCode()), String.valueOf(rd.getRateDate()) });
    }
    
    /*
     * Deleting a RateDetails
     */
    public void deleteRateDetails(RateDetails rd) {
        db = this.getWritableDatabase();
        db.delete(TABLE_RATE_DETAILS, RD_SOURCE_CURRENCY_CODE + " = ?," + RD_TARGET_CURRENCY_CODE + " = ?," + RD_RATE_DATE + " = ?",
                new String[] { String.valueOf(rd.getSourceCurrencyCode()), String.valueOf(rd.getTargetCurrencyCode()), String.valueOf(rd.getRateDate()) });
    }
    
 // closing database
    public void closeDB() {
        db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
