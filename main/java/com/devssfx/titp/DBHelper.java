package com.devssfx.titp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "ssfxTITP";


    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public ArrayList<ThisPlace> LoadPlaceList(){
        ArrayList<ThisPlace> rtn = new ArrayList<ThisPlace>();
        ThisPlace placeItem;

        Cursor _data = null;
        SQLiteDatabase db = this.getWritableDatabase();

        _data = db.rawQuery("select * from PlaceList order by PlaceDeviceId desc", new String[]{});

        while(_data.moveToNext()){
            placeItem = new ThisPlace();
            placeItem.PlaceDeviceId = _data.getInt(_data.getColumnIndex("PlaceDeviceId"));
            placeItem.PlaceId = _data.getString(_data.getColumnIndex("PlaceId"));
            placeItem.PlaceCode = _data.getString(_data.getColumnIndex("PlaceCode"));
            //placeItem.PlaceUserId = _data.getString(_data.getColumnIndex("PlaceUserId"));
            placeItem.PlaceName = _data.getString(_data.getColumnIndex("PlaceName"));
            placeItem.PlaceDate = _data.getInt(_data.getColumnIndex("PlaceDate"));
            placeItem.CreateDate = _data.getInt(_data.getColumnIndex("CreateDate"));
            placeItem.PlaceAddress = _data.getString(_data.getColumnIndex("PlaceAddress"));
            placeItem.PlaceLat = _data.getString(_data.getColumnIndex("PlaceLat"));
            placeItem.PlaceLong = _data.getString(_data.getColumnIndex("PlaceLong"));
            placeItem.ModCount = _data.getInt(_data.getColumnIndex("ModCount"));

            rtn.add(placeItem);
        }
        _data.close();
        db.close();

        return rtn;
    }

    public int CreatePlace(String password, String placeName){
        int rtn = 0;

        SQLiteDatabase db = this.getWritableDatabase();
        String ss = "insert into PlaceList (PlaceName, ModCount, IsAdmin, IsDeleted) values (" + ParseString(placeName) + ", 0, 1, 0)";
        try {
            db.execSQL(ss);
            Cursor data = db.rawQuery("select MAX(PlaceDeviceId) as PlaceDeviceId from PlaceList", new String[]{});
            if(data.moveToFirst()){
                rtn = data.getInt(0);
            }
            data.close();
        }catch(Exception ex){
            rtn = -1;
        }

        db.close();
        return rtn;

    }
    public int PlaceDelete(String placeCode){
        int rtn = 0;

        SQLiteDatabase db = this.getWritableDatabase();
        String ss;

        try {
            ss = "delete from PlaceList where PlaceCode is null";
            db.execSQL(ss);
            if(placeCode != null) {
                ss = "delete from PlaceList where PlaceCode = " + ParseString(placeCode);
                db.execSQL(ss);
            }
            rtn = 1;
        }catch(Exception ex){
            rtn = -1;
        }
        db.close();
        return rtn;

    }
    public int PlaceDeleteLocalOnly(Integer localId){
        int rtn = 0;

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("delete from PlaceList where PlaceDeviceId = " + localId);
            rtn = 1;
        }catch(Exception ex){
            rtn = -1;
        }
        db.close();
        return rtn;

    }

    public int PlaceUpdateCode(String localId, String placeId, String placeCode, int modCount){
        int rtn = 0;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("update PlaceList set PlaceCode = " + ParseString(placeCode) + ", placeId = " + ParseString(placeId)
                    + ", ModCount = " + modCount
                    + " where PlaceDeviceId = " + localId);
            rtn = 1;
        }finally {
            db.close();
        }
        return rtn;
    }


    public int PlaceSaveOnFind(ThisPlace place){
        int rtn = 0;
        Boolean found = false;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor data = db.rawQuery("select PlaceCode from PlaceList where PlaceCode = " + ParseString(place.PlaceCode), new String[]{} );
            if(data.moveToFirst()){
                if(data.getString(0).equals(place.PlaceCode)){
                    found = true;
                }
            }
            data.close();

            String sql;
            if(found){
                sql = "update PlaceList set PlaceId = " + ParseString(place.PlaceId) + ", PlaceName = " + ParseString(place.PlaceName)
                        + ", PlaceDate = " + place.PlaceDate + ", PlaceAddress = " + ParseString(place.PlaceAddress) + ", PlaceLat = " + ParseString(place.PlaceLat) + ", PlaceLong = " + ParseString(place.PlaceLong)
                        + ", CreateDate = " + place.CreateDate
                        + ", ModCount = " + place.ModCount
                        + " where PlaceCode = " + ParseString(place.PlaceCode);
            }else{
                sql = "insert into PlaceList (PlaceId, PlaceCode, PlaceName, PlaceDate, CreateDate, PlaceAddress, PlaceLat, PlaceLong, ModCount, IsAdmin, IsDeleted) values ("
                        + ParseString(place.PlaceId) + "," + ParseString(place.PlaceCode) + "," + ParseString(place.PlaceName) + "," + place.PlaceDate + "," + place.CreateDate
                        + "," + ParseString(place.PlaceAddress) + "," + ParseString(place.PlaceLat) + "," + ParseString(place.PlaceLong)
                        + "," + place.ModCount + ",0,0"
                        +")";
            }

            db.execSQL(sql);
            rtn = 1;
        }finally {
            db.close();
        }
        return rtn;
    }

    public ThisPlace LoadPlaceFromCode(String placeCode){
        ThisPlace placeItem = null;

        Cursor _data = null;
        SQLiteDatabase db = this.getWritableDatabase();
        _data = db.rawQuery("select * from PlaceList where PlaceCode = " + ParseString(placeCode), new String[]{});
        if (_data.moveToNext()){
            placeItem = new ThisPlace();
            placeItem.PlaceDeviceId = _data.getInt(_data.getColumnIndex("PlaceDeviceId"));
            placeItem.PlaceId = _data.getString(_data.getColumnIndex("PlaceId"));
            placeItem.PlaceCode = _data.getString(_data.getColumnIndex("PlaceCode"));
            //placeItem.PlaceUserId = _data.getString(_data.getColumnIndex("PlaceUserId"));
            placeItem.PlaceName = _data.getString(_data.getColumnIndex("PlaceName"));
            placeItem.PlaceDate = _data.getInt(_data.getColumnIndex("PlaceDate"));
            placeItem.CreateDate = _data.getInt(_data.getColumnIndex("CreateDate"));
            placeItem.PlaceAddress = _data.getString(_data.getColumnIndex("PlaceAddress"));
            placeItem.PlaceLat = _data.getString(_data.getColumnIndex("PlaceLat"));
            placeItem.PlaceLong = _data.getString(_data.getColumnIndex("PlaceLong"));
            placeItem.ModCount = _data.getInt(_data.getColumnIndex("ModCount"));
            placeItem.IsAdmin = _data.getInt(_data.getColumnIndex("IsAdmin"));
            if(placeItem.IsAdmin == null)
                placeItem.IsAdmin = 0;
        }
        _data.close();
        db.close();

        return placeItem;
    }

    public boolean UpdateLocation(ThisPlace place){
        boolean rtn = false;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            //Integer modDate, modTime;

            db.execSQL("update PlaceList set PlaceLat = " + ParseString(place.PlaceLat) + ", PlaceLong = " + ParseString(place.PlaceLong)
                    + ", ModCount = " + place.ModCount
                    + " where PlaceCode = " + ParseString(place.PlaceCode));
            rtn = true;

        }finally {
            db.close();
        }
        return rtn;
    }

    public boolean EnableAdmin(String placeCode){
        Boolean success = false;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("update PlaceList set IsAdmin = 1 where PlaceCode = " + ParseString(placeCode));
            success = true;
        }finally {
            db.close();
        }
        return success;
    }

    private String ParseString(String s){
        if(s == null)
            return s;
        else
            return "'" + s.replace("'","''") + "'";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        if (TableExists("PlaceList", db)) {
                db.execSQL("DROP TABLE IF EXISTS plTmp");

                db.execSQL("CREATE TABLE plTmp (PlaceDeviceId INTEGER PRIMARY KEY, PlaceId TEXT, PlaceCode TEXT"
                        + ", PlaceName TEXT, PlaceDate INTEGER, CreateDate INTEGER, PlaceAddress TEXT, PlaceLat TEXT, PlaceLong TEXT" +
                        ", ModCount INTEGER, IsAdmin INTEGER, IsDeleted INTEGER);");

                db.execSQL("insert into plTmp select * from PlaceList");
                db.execSQL("alter table plTmp rename to PlaceCode");
        }else {
            db.execSQL("CREATE TABLE PlaceList (PlaceDeviceId INTEGER PRIMARY KEY, PlaceId TEXT, PlaceCode TEXT"
                    + ", PlaceName TEXT, PlaceDate INTEGER, CreateDate INTEGER, PlaceAddress TEXT, PlaceLat TEXT, PlaceLong TEXT" +
                    ", ModCount INTEGER, IsAdmin INTEGER, IsDeleted INTEGER);");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS PlaceList");

            onCreate(db);

    }

    private boolean TableExists(String tblName, SQLiteDatabase db){
        boolean tblExists = false;
        Cursor _data = null;
        _data = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"+ tblName +"'", new String[]{});
        if(_data.moveToNext()){
            tblExists = true;
        }
        _data.close();

        return tblExists;

    }

}
