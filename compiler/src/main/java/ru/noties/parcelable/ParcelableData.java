package ru.noties.parcelable;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
class ParcelableData {

    final Element element;
    final List<ParcelableItem> items;

    ParcelableData(Element element, List<ParcelableItem> items) {
        this.element = element;
        this.items = Collections.unmodifiableList(items);
    }
}
