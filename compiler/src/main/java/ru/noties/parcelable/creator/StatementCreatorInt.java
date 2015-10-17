package ru.noties.parcelable.creator;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorInt extends StatementCreatorBase {

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createIntArray" : "readInt";
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeIntArray" : "writeInt";
    }
}
