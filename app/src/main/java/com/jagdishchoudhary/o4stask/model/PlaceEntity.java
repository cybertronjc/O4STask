package com.jagdishchoudhary.o4stask.model;

import androidx.annotation.Keep;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
@Keep
public class PlaceEntity {

    @PrimaryKey(autoGenerate = true)
    private int place_id;

    @ColumnInfo(name = "places") // column name will be "places" table

    String name;
    String address;
    double lat;
    double lng;

    public PlaceEntity(int place_id, String name, String address, double lat, double lng) {
        this.place_id = place_id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public int getPlace_id() {
        return place_id;
    }

    public void setPlace_id(int place_id) {
        this.place_id = place_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceEntity)) return false;

        PlaceEntity place = (PlaceEntity) o;

        if (place_id != place.place_id) return false;
        return name != null ? name.equals(place.name) : place.name == null;
    }



    @Override
    public int hashCode() {
        int result = place_id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "place{" +
                "place_id=" + place_id +
                ", content='" + name + '\'' +
                ", title='" + name + '\'' +
                '}';
    }
}
