package ru.noties.parcelable;

/**
 * Created by Dimitry Ivanov on 12.10.2015.
 */
class ParcelableItem {

    final String name;
    final ParcelableType type;

    ParcelableItem(String name, ParcelableType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ParcelableItem{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
