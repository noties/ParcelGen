package ru.noties.parcelable;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
class ParcelableDataParser {

    final ParcelableLogger mLogger;
    final Types mTypes;
    final Elements mElements;

    ParcelableDataParser(ParcelableLogger logger, Types types, Elements elements) {
        this.mLogger = logger;
        this.mTypes = types;
        this.mElements = elements;
    }

    ParcelableData parse(Element element) {

        if (!(element instanceof TypeElement)) {
            return null;
        }

        final TypeElement typeElement = (TypeElement) element;
        final List<? extends Element> enclosed = typeElement.getEnclosedElements();
        if (enclosed == null
                || enclosed.size() == 0) {
            return null;
        }

        final List<ParcelableItem> items = new ArrayList<>();

        TypeMirror typeMirror;
        boolean isArray;
        ParcelableItem item;
        ParcelableType type;
        String name;

        for (Element encl: enclosed) {

            if (encl.getKind() != ElementKind.FIELD) {
                continue;
            }

            typeMirror = encl.asType();
            isArray = false;

            if (typeMirror.getKind() == TypeKind.ARRAY) {
                final ArrayType arrayType = (ArrayType) typeMirror;
                typeMirror = arrayType.getComponentType();
                isArray = true;
            }

            name = encl.getSimpleName().toString();

            type = convertType(typeMirror);

            // check if field's type is annotated with parcelable
            if (type == null && isParcelableAnnotationPresent(encl)) {
                type = ParcelableType.PARCELABLE;
            }

            if (type == null) {
                mLogger.log(Diagnostic.Kind.NOTE, "Could not parse `%s`. It won't be included in modified `%s`", typeMirror, typeElement);
                continue;
            }

            if (isArray
                    && (type == ParcelableType.ENUM || type == ParcelableType.SERIALIZABLE)) {
                mLogger.log(Diagnostic.Kind.NOTE, "There is not support for arrays of enums or Serializable objects. Field: %s (%s) in class: %s", name, encl, typeElement);
                continue;
            }

            if (isArray) {
                item = new ParcelableItemArray(name, ParcelableType.ARRAY, type);
            } else {
                item = new ParcelableItem(name, type);
            }

            items.add(item);
        }

        if (items.size() == 0) {
            return null;
        }

        return new ParcelableData(element, items);
    }

    private ParcelableType convertType(TypeMirror mirror) {

        switch (mirror.getKind()) {

            case BYTE:
                return ParcelableType.BYTE;

            case INT:
                return ParcelableType.INT;

            case LONG:
                return ParcelableType.LONG;

            case FLOAT:
                return ParcelableType.FLOAT;

            case DOUBLE:
                return ParcelableType.DOUBLE;

            case BOOLEAN:
                return ParcelableType.BOOLEAN;

            case DECLARED:
                // might be - String, Enum, Parcelable, Serializable?
                final String str = mirror.toString();
                if ("java.lang.String".equals(str)) {
                    return ParcelableType.STRING;
                }

                if (isEnum(mirror)) {
                    return ParcelableType.ENUM;
                }

                if (isSubtype(mirror, "android.os.Parcelable")) {
                    return ParcelableType.PARCELABLE;
                }

                if (isSubtype(mirror, "java.io.Serializable")) {
                    return ParcelableType.SERIALIZABLE;
                }

                return null;


            default:
                return null;
        }
    }

    private boolean isParcelableAnnotationPresent(Element field) {

        TypeMirror mirror = field.asType();

        if (mirror.getKind() == TypeKind.ARRAY) {
            mirror = ((ArrayType) mirror).getComponentType();
        }

        if (!(mirror instanceof DeclaredType)) {
            return false;
        }

        final Element type = ((DeclaredType) mirror).asElement();

        for (AnnotationMirror annotationMirror: type.getAnnotationMirrors()) {
            if (ParcelableProcessor.ANNOTATION.equals(annotationMirror.getAnnotationType().toString())) {
                return true;
            }
        }

        return false;
    }

    private boolean isEnum(TypeMirror mirror) {

        final DeclaredType declaredType = (mirror instanceof DeclaredType) ? (DeclaredType) mirror : null;

        if (declaredType == null) {
            return false;
        }

        final Element element = declaredType.asElement();

        if (!(element instanceof TypeElement)) {
            return false;
        }
        final TypeMirror superMirror = ((TypeElement) element).getSuperclass();

        return superMirror.toString().startsWith("java.lang.Enum");
    }

    private boolean isSubtype(TypeMirror typeMirror, String toCheck) {
        final TypeElement typeElement = mElements.getTypeElement(toCheck);
        return mTypes.isAssignable(typeMirror, typeElement.asType());
    }
}
