package ru.noties.parcelable;

import javax.tools.Diagnostic;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
public interface ParcelableLogger {
    void log(Diagnostic.Kind level, String message, Object... args);
}
