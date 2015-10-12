package ru.noties.parcelable;

/**
 * Created by Dimitry Ivanov on 12.10.2015.
 */
class ParcelableItemArray extends ParcelableItem {

    final ParcelableType arrayType;

    ParcelableItemArray(String name, ParcelableType type, ParcelableType arrayType) {
        super(name, type);
        this.arrayType = arrayType;
    }
}
