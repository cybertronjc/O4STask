package com.jagdishchoudhary.o4stask.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.jagdishchoudhary.o4stask.model.PlaceDao;
import com.jagdishchoudhary.o4stask.model.PlaceEntity;

@Database(entities = {PlaceEntity.class}, version =  1)
public abstract class PlaceDatabase extends RoomDatabase {


    public abstract PlaceDao getPlaceDao();

    private static PlaceDatabase placesDb;

    public static PlaceDatabase getInstance(Context context) {
        if (null == placesDb) {
            placesDb = buildDatabaseInstance(context);
        }
        return placesDb;
    }

    private static PlaceDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                PlaceDatabase.class,
                "places")
                .allowMainThreadQueries().build();
    }

    public void cleanUp(){
        placesDb = null;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }
}
