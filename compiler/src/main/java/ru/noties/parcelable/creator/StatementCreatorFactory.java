package ru.noties.parcelable.creator;

import java.util.HashMap;
import java.util.Map;

import ru.noties.parcelable.ParcelableType;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
public class StatementCreatorFactory {

    private final Map<ParcelableType, StatementCreator> mCache;

    public StatementCreatorFactory() {
        this.mCache = new HashMap<>();
    }

    public StatementCreator get(ParcelableType type) {
        final StatementCreator creator;
        if (mCache.containsKey(type)) {
            creator = mCache.get(type);
        } else {
            creator = createNew(type);
            mCache.put(type, creator);
        }
        return creator;
    }

    private static StatementCreator createNew(ParcelableType type) {

        switch (type) {

            case BYTE:
                return new StatementCreatorByte();

            case INT:
                return new StatementCreatorInt();

            case LONG:
                return new StatementCreatorLong();

            case FLOAT:
                return new StatementCreatorFloat();

            case DOUBLE:
                return new StatementCreatorDouble();

            case SERIALIZABLE:
                return new StatementCreatorSerializable();

            case STRING:
                return new StatementCreatorString();

            case BUNDLE:
                return new StatementCreatorBundle();

            case CHAR_SEQUENCE:
                return new StatementCreatorCharSequence();

            case TYPED_LIST:
                return new StatementCreatorTypedList();

            case PARCELABLE:
                return new StatementCreatorParcelable();

            case BOOLEAN:
                return new StatementCreatorBoolean();

            case ENUM:
                return new StatementCreatorEnum();

            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }
}
