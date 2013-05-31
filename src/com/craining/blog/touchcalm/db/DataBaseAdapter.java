package com.craining.blog.touchcalm.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseAdapter {

	public static final String KEY_ID = "_id";
	public static final String KEY_STYLE = "style";
	public static final String KEY_DOWN = "down";
	public static final String KEY_UP = "up";
	public static final String KEY_EFFECTIVE = "effective";
	private static final String DB_NAME = "touchcalm.db";
	private static final String DB_TABLE = "table_calm";
	private static final int DB_VERSION = 1;
	private Context mContext = null;
	
	private static SQLiteDatabase mSQLiteDatabase = null;
	private DatabaseHelper mDatabaseHelper = null;

	public DataBaseAdapter(Context context) {
		mContext = context;
	}

	public void open() throws SQLException {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	public void close() {
//		mSQLiteDatabase.close();
		mDatabaseHelper.close();
	}

	/**
	 * ����һ������
	 * @param style
	 * @param down
	 * @param up
	 * @return
	 */
	public long insertData(String style, String down, String up, String effective) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_STYLE, style);
		initialValues.put(KEY_DOWN, down);
		initialValues.put(KEY_UP, up);
		initialValues.put(KEY_EFFECTIVE, effective);

		return mSQLiteDatabase.insert(DB_TABLE, KEY_ID, initialValues);
	}

	/**
	 * ��ȡ����� column �е��������ݷŵ�һ��List��
	 * 
	 * @param column
	 * @return
	 */
	public static ArrayList<String> getColumnThingsInf(String column) {
		ArrayList<String> getlist = new ArrayList<String>();
		Cursor findColumDate = mSQLiteDatabase.query(DB_TABLE, new String[] { column },
				null, null, null, null, null);
		findColumDate.moveToFirst();
		final int Index = findColumDate.getColumnIndexOrThrow(column);
		for (findColumDate.moveToFirst(); !findColumDate.isAfterLast(); findColumDate.moveToNext()) {
			String getOneItem = findColumDate.getString(Index);
			getlist.add(getOneItem);
		}
		return getlist;
	}
	
	/**
	 * ɾ���������
	 * @param db
	 */
	public void clearTable() {
		String DB_DELETE = "delete from " + DB_TABLE +";";
		mSQLiteDatabase.execSQL(DB_DELETE);
	}

	/**
	 * �ж��Ƿ�Ϊ��
	 * @param dbadapter
	 * @return
	 */
	public boolean isEmpty(DataBaseAdapter dbadapter) {
		
		return (mSQLiteDatabase.query(DB_TABLE, new String[] { DataBaseAdapter.KEY_STYLE },
				null, null, null, null, null) == null);
	}
	
	public boolean upDateDB(String style, String downSize, String upSize, String effective) {
		String sql = "UPDATE " + DB_TABLE + " SET " + KEY_DOWN + " = '" + downSize + "', " + KEY_UP + " = '" + upSize + "', " + KEY_EFFECTIVE + "='" + effective + 
		"' WHERE " + KEY_STYLE  + "='" + style + "');";
		mSQLiteDatabase.execSQL(sql);
		
		return true;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			/* ������getWritableDatabase()�� getReadableDatabase()����ʱ �򴴽�һ�����ݿ�*/
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/* ���ݿ�û�б�ʱ����һ�� */
			String DB_CREATE = "CREATE TABLE " + DB_TABLE + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY," 
			+ KEY_STYLE + " TEXT," 
			+ KEY_DOWN + " TEXT," 
			+ KEY_UP + " TEXT," 
			+ KEY_EFFECTIVE + " TEXT )";
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
}
