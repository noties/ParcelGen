package ru.noties.parcelable.creator;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorByte extends StatementCreatorBase {

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createByteArray" : "readByte";
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeByteArray" : "writeByte";
    }
}
