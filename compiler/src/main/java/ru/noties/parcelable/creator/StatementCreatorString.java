package ru.noties.parcelable.creator;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorString extends StatementCreatorBase {

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createStringArray" : "readString";
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeStringArray" : "writeString";
    }
}
