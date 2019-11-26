package com.jagdishchoudhary.o4stask.model;

import androidx.annotation.Keep;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jagdishchoudhary.o4stask.model.PlaceEntity;

import java.util.List;

@Dao
@Keep
public interface PlaceDao {
    @Query("SELECT * FROM placeentity "+ "places")
    List<PlaceEntity> getAll();


    /*
     * Insert the object in database
     * @param note, object to be inserted
     */
    @Insert
    void insert(PlaceEntity placeEntity);

    /*
     * update the object in database
     * @param note, object to be updated
     */
    @Update
    void update(PlaceEntity repos);

    /*
     * delete the object from database
     * @param note, object to be deleted
     */
    @Delete
    void delete(PlaceEntity placeEntity);

    /*
     * delete list of objects from database
     * @param note, array of objects to be deleted
     */
    @Delete
    void delete(PlaceEntity... placeEntities);      // Note... is varargs, here placeEntities is an array

}
