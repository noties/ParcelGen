package ru.noties.parcelable.creator;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorFloat extends StatementCreatorBase {

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createFloatArray" : "readFloat";
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeFloatArray" : "writeFloat";
    }
}
