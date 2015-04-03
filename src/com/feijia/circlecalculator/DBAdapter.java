package com.feijia.circlecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteOpenHelper; 
import android.util.Log;
public class DBAdapter {
	static final String KEY_ROWID = "_id"; 
	static final String KEY_EQUATION = "equation"; 
	static final String KEY_ANSWER = "answer"; 
	static final String TAG = "DBAdapter";
	static final String DATABASE_NAME = "CircleCalc"; 
	static final String DATABASE_TABLE = "EquationHistory";
	static final int DATABASE_VERSION = 1;

	static final String DATABASE_CREATE =
			"create table EquationHistory (_id integer primary key autoincrement, " + "equation text not null, answer text not null, reserved01 text, reserved02 text, reserved03 text);";
	static final String TABLE_TWO = "LastEquation";
	static final String DISEQUA = "displayequation";
	static final String OFFSET = "offset";
	static final String MODIFY = "modified";
	static final String ANSDIS = "answer";
	static final String ERR = "error";
	static final String ANSBUFF = "AnsBuff";
	static final String TABLE_TWO_CREATE =
			"create table LastEquation (_id integer primary key autoincrement, " + "displayequation text, offset text not null, modified text not null, answer text, error text, AnsBuff text, reserved1 text, reserved2 text, reserved3 text);";
	final Context context;
	DatabaseHelper DBHelper;
	SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
			DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION); 
			}
			
		@Override	
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE); 
				db.execSQL(TABLE_TWO_CREATE); 
			} 
			catch (SQLException e) {
				e.printStackTrace(); 
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS EquationHistory");
			db.execSQL("DROP TABLE IF EXISTS LastEquation");
			onCreate(db); 
		}
	}
	
	//---opens the database---
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this; 
	}
	//---closes the database---
	public void close() {
		DBHelper.close(); 
	}
	//---insert a EquationHistory into the database---
	public long insertEquationHistory(String equation) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EQUATION, equation);
		initialValues.put(KEY_ANSWER, " "); //Answer col Reserved for further development
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	//---deletes a particular EquationHistory---
	public boolean deleteEquationHistory(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0; 
	}
	//---retrieves all the EquationHistorys---
	public Cursor getAllEquationHistorys(String Num) {
		//db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EQUATION, KEY_ANSWER}, null, null, null, null, KEY_ROWID+" DESC", Num);
	}
	//---retrieves an EquationHistory with offset---
	public Cursor getEquationOffset(int off) {
		//db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		String offsets = Integer.toString(off);
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EQUATION, KEY_ANSWER}, null, null, null, null, KEY_ROWID+" DESC", offsets+",1");
	}
	//---retrieves a particular EquationHistory---
	public Cursor getEquationHistory(long rowId) throws SQLException {
		Cursor mCursor =
			//db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal)
			db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EQUATION, KEY_ANSWER}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) { 
			mCursor.moveToFirst();
		}
		return mCursor; 
	}
	// get table one row count
	public int getHistoryCount() {
	    String countQuery = "SELECT  * FROM " + DATABASE_TABLE;
	    Cursor cursor = db.rawQuery(countQuery, null);
	    int cnt = cursor.getCount();
	    cursor.close();
	    return cnt;
	}
	//---updates a EquationHistory---
	public boolean updateEquationHistory(long rowId, String equation, String answer) {
		ContentValues args = new ContentValues();
		args.put(KEY_EQUATION, equation);
		args.put(KEY_ANSWER, answer);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	//---methods for table two---
	//---insert--
	public long preserveEquation(String displayequation, String offset, String modify, String answer, String isError, String answerbuffer) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DISEQUA, displayequation);
		initialValues.put(OFFSET, offset);
		initialValues.put(MODIFY, modify);
		initialValues.put(ANSDIS, answer);
		initialValues.put(ERR, isError);
		initialValues.put(ANSBUFF, answerbuffer);
		return db.insert(TABLE_TWO, null, initialValues);
	}
	//---retrive latest equation---
	public Cursor returnEquation() {
		//db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		return db.query(TABLE_TWO, new String[] {KEY_ROWID, DISEQUA, OFFSET, MODIFY, ANSDIS, ERR, ANSBUFF}, null, null, null, null, KEY_ROWID+" DESC", "1");
	}
	public void DeleteLast(){
		String maxid = KEY_ROWID + "=" + "(SELECT MAX("+KEY_ROWID+") FROM " + TABLE_TWO + ")";
		db.delete(TABLE_TWO, maxid ,null);
	}
}
