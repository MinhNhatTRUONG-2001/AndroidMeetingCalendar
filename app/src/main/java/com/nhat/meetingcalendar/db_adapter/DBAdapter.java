package com.nhat.meetingcalendar.db_adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.nhat.meetingcalendar.R;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class DBAdapter {
    private static String[] tableColumnNames, tableColumnTypes, imgTableColumnNames, imgTableColumnTypes;
    private static  String TAG;
    private static String dbName;
    private static String tableName;
    private static String imgTableName;
    private static final int DATABASE_VERSION = 1;
    private static String TABLE_CREATE_QUERY;
    private static String TABLE_DELETE_QUERY;
    private static String IMG_TABLE_CREATE_QUERY;
    private static String IMG_TABLE_DELETE_QUERY;
    private Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqlLiteDb;

    //Here we define the constructor
    public DBAdapter(Context context) {
        this.context = context;
        DBAdapter.dbName = context.getString(R.string.db_name);
        DBAdapter.tableName = context.getString(R.string.table_name);
        DBAdapter.imgTableName = context.getString(R.string.img_table_name);
        tableColumnNames = context.getResources().getStringArray(R.array.table_column_names);
        tableColumnTypes = context.getResources().getStringArray(R.array.table_column_types);
        imgTableColumnNames = context.getResources().getStringArray(R.array.img_table_column_names);
        imgTableColumnTypes = context.getResources().getStringArray(R.array.img_table_column_types);
        TAG = context.getString(R.string.db_class_tag);
        TABLE_CREATE_QUERY = "CREATE TABLE " + tableName + " ("
                + tableColumnNames[0] + "  " + tableColumnTypes[0] + " primary key, "
                + tableColumnNames[1] + "  " + tableColumnTypes[1] + " not null, "
                + tableColumnNames[2] + "  " + tableColumnTypes[2] + " not null, "
                + tableColumnNames[3] + "  " + tableColumnTypes[3] + " not null, "
                + tableColumnNames[4] + "  " + tableColumnTypes[4] + " not null, "
                + tableColumnNames[5] + "  " + tableColumnTypes[5] + " );";
        TABLE_DELETE_QUERY = "DROP TABLE IF EXISTS " + tableName;
        IMG_TABLE_CREATE_QUERY = "CREATE TABLE " + imgTableName + " ("
                + imgTableColumnNames[0] + "  " + imgTableColumnTypes[0] + " primary key, "
                + imgTableColumnNames[1] + "  " + imgTableColumnTypes[1] + " not null, "
                + imgTableColumnNames[2] + "  " + imgTableColumnTypes[2] + " not null, "
                + imgTableColumnNames[3] + "  " + imgTableColumnTypes[3] + " );";
        IMG_TABLE_DELETE_QUERY = "DROP TABLE IF EXISTS " + imgTableName;
        dbHelper = new DatabaseHelper(context);
    }

    //Here we define the DatabaseHelper class
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, dbName, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
             try {
                 db.execSQL(TABLE_CREATE_QUERY);
                 db.execSQL(IMG_TABLE_CREATE_QUERY);
             }
             catch(SQLException e) {
                e.printStackTrace();
             }
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, new DBAdapter().context.getString(R.string.version_upgrade) + oldVersion + " to "
                    + newVersion + ". " + new DBAdapter().context.getString(R.string.warning_data_delete));
            //Here we remove the table
            db.execSQL(TABLE_DELETE_QUERY);
            db.execSQL(IMG_TABLE_DELETE_QUERY);
            //Here we create the table again
            onCreate(db);
        }
    }

    public DBAdapter(){}
    //This method will open the database
    public DBAdapter openDBConnection() {
        sqlLiteDb = dbHelper.getWritableDatabase();
        return this;
    }
    //This method will close the database
    public void closeDBConnection() {
        dbHelper.close();
    }

    //Here we add a new meeting to the database
    public long addMeeting(String title, String place, String participants, String datetime, Bitmap image) {
        long newId;
        openDBConnection();
        ContentValues initialValues = new ContentValues();
        initialValues.put(tableColumnNames[1], title);
        initialValues.put(tableColumnNames[2], place);
        initialValues.put(tableColumnNames[3], participants);
        initialValues.put(tableColumnNames[4], datetime);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean compressResult = true;
        if (image != null) {
            compressResult = image.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        }
        if (compressResult) {
            //Here we set the image to be saved under image column
            initialValues.put(tableColumnNames[5], outputStream.toByteArray());
            newId = sqlLiteDb.insert(tableName, null, initialValues);
        } else {
            newId = -1;
        }
        closeDBConnection();
        return newId;
    }

    public boolean deleteMeeting(long rowID) {
        openDBConnection();
        boolean isDeleted = sqlLiteDb.delete(tableName, tableColumnNames[0] + "=" + rowID, null) > 0;
        closeDBConnection();
        return isDeleted;
    }

    private Vector<Object[]> getRowData(Cursor cursor){
        Object[] dataRow;
        Vector<Object[]> dataRows = new Vector<>();
        byte[] imgByte;
        ImageView imageView;
        //Here we go through each row of the result set and copy data of each row to an Object array
        if(cursor.moveToFirst()) {
            do {
                dataRow = new Object[cursor.getColumnCount()];
                dataRow[0] = cursor.getInt(0);
                dataRow[1] = cursor.getString(1);
                dataRow[2] = cursor.getString(2);
                dataRow[3] = cursor.getString(3);
                dataRow[4] = cursor.getString(4);
                imgByte = cursor.getBlob(5);
                if(imgByte != null) {
                    imageView = new ImageView(context);
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
                    dataRow[5] = imageView;
                }
                dataRows.add(dataRow);
            } while(cursor.moveToNext());
        }
        return dataRows;
    }

    //This method will retrieve all meetings
    public Vector<Object[]> getAllMeetings() {
        openDBConnection();
        Cursor cursor = sqlLiteDb.query(tableName, new String[]{tableColumnNames[0], tableColumnNames[1], tableColumnNames[2], tableColumnNames[3], tableColumnNames[4], tableColumnNames[5]},
                null, null, null, null, null);
        Vector<Object[]> dataRows = getRowData(cursor);
        closeDBConnection();
        return dataRows;
    }

    //This method will update a meeting
    public boolean updateMeeting(long rowID, String title, String place, String participants, String datetime, Bitmap image) {
        boolean isUpdated;
        openDBConnection();
        ContentValues values = new ContentValues();
        values.put(tableColumnNames[1], title);
        values.put(tableColumnNames[2], place);
        values.put(tableColumnNames[3], participants);
        values.put(tableColumnNames[4], datetime);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean compressionResult = true;
        if (image != null) {
            compressionResult = image.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        }
        if (compressionResult) {
            //Here we add image byte array to the set of data which will be written to the table
            values.put(tableColumnNames[5], outputStream.toByteArray());
            isUpdated = (sqlLiteDb.update(tableName, values, tableColumnNames[0] + "=" + rowID, null) > 0);
        }
        else {
            isUpdated = false;
        }
        closeDBConnection();
        return isUpdated;
    }

    //This part is for participant image handling

    public long addParticipantImage(long meetingsId, String participant, Bitmap image) {
        long newId;
        openDBConnection();
        ContentValues initialValues = new ContentValues();
        initialValues.put(imgTableColumnNames[1], meetingsId);
        initialValues.put(imgTableColumnNames[2], participant);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean compressResult = true;
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        }
        if (compressResult) {
            //Here we set the image to be saved under image column
            initialValues.put(imgTableColumnNames[3], outputStream.toByteArray());
            newId = sqlLiteDb.insert(imgTableName, null, initialValues);
        } else {
            newId = -1;
        }
        closeDBConnection();
        return newId;
    }

    public boolean deleteParticipantImageByMeetingsId(long meetingsId) {
        openDBConnection();
        boolean isDeleted = sqlLiteDb.delete(imgTableName, imgTableColumnNames[1] + "=" + meetingsId, null) > 0;
        closeDBConnection();
        return isDeleted;
    }

    private Vector<Object[]> getImgRowData(Cursor cursor){
        Object[] dataRow;
        Vector<Object[]> dataRows = new Vector<>();
        byte[] imgByte;
        ImageView imageView;
        //Here we go through each row of the result set and copy data of each row to an Object array
        if(cursor.moveToFirst()) {
            do {
                dataRow = new Object[cursor.getColumnCount()];
                dataRow[0] = cursor.getInt(0);
                dataRow[1] = cursor.getInt(1);
                dataRow[2] = cursor.getString(2);
                imgByte = cursor.getBlob(3);
                if(imgByte != null) {
                    imageView = new ImageView(context);
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
                    dataRow[3] = imageView;
                }
                dataRows.add(dataRow);
            } while(cursor.moveToNext());
        }
        return dataRows;
    }

    public Vector<Object[]> getAllParticipantImages() {
        openDBConnection();
        Cursor cursor = sqlLiteDb.query(imgTableName, new String[]{imgTableColumnNames[0], imgTableColumnNames[1], imgTableColumnNames[2], imgTableColumnNames[3]},
                null, null, null, null, null);
        Vector<Object[]> dataRows = getImgRowData(cursor);
        closeDBConnection();
        return dataRows;
    }
}