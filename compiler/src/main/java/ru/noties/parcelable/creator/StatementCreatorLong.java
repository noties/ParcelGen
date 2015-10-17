package ru.noties.parcelable.creator;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorLong extends StatementCreatorBase {

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createLongArray" : "readLong";
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeLongArray" : "writeLong";
    }
}
