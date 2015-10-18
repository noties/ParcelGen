package ru.noties.parcelable;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
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

    static final String ANDROID_OS_BUNDLE       = "android.os.Bundle";
    static final String ANDROID_OS_PARCELABLE   = "android.os.Parcelable";
    static final String JAVA_IO_SERIALIZABLE    = "java.io.Serializable";
    static final String JAVA_LANG_CHAR_SEQUENCE = "java.lang.CharSequence";
    static final String JAVA_LANG_ENUM          = "java.lang.Enum";
    static final String JAVA_LANG_STRING        = "java.lang.String";
    static final String JAVA_UTIL_LIST          = "java.util.List";
    static final String JAVA_UTIL_ARRAY_LIST    = "java.util.ArrayList";

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

            // skip field if it's marked as transient
            if (encl.getModifiers().contains(Modifier.TRANSIENT)) {
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

            if (type == null) {
                mLogger.log(Diagnostic.Kind.NOTE, "Could not parse `%s`. " +
                        "It won't be included in modified `%s`", typeMirror, typeElement);
                continue;
            }

            if (isArray
                    && (type == ParcelableType.ENUM
                            || type == ParcelableType.SERIALIZABLE
                            || type == ParcelableType.TYPED_LIST
                            || type == ParcelableType.LIST
                            || type == ParcelableType.BUNDLE)
            ) {
                mLogger.log(Diagnostic.Kind.NOTE, "There is not support for arrays of Enum, " +
                        "Serializable, Bundle or List objects. Field: %s (%s) in class: %s", name, encl, typeElement);
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

        return new ParcelableData(element, items, shouldCallSuper(typeElement));
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
                if (JAVA_LANG_STRING.equals(str)) {
                    return ParcelableType.STRING;
                }

                if (isSubtype(mirror, JAVA_LANG_CHAR_SEQUENCE)) {
                    return ParcelableType.CHAR_SEQUENCE;
                }

                if (ANDROID_OS_BUNDLE.equalsIgnoreCase(str)) {
                    return ParcelableType.BUNDLE;
                }

                // check if it's a list
                if (isParcelableList(mirror)) {
                    return ParcelableType.TYPED_LIST;
                }

                if (isList(mirror)) {
                    return ParcelableType.LIST;
                }

                if (isEnum(mirror)) {
                    return ParcelableType.ENUM;
                }

                if (isSubtype(mirror, ANDROID_OS_PARCELABLE)) {
                    return ParcelableType.PARCELABLE;
                }

                if (isSubtype(mirror, JAVA_IO_SERIALIZABLE)) {
                    return ParcelableType.SERIALIZABLE;
                }

                if (isParcelableAnnotationPresent(mirror)) {
                    return ParcelableType.PARCELABLE;
                }

                return ParcelableType.OBJECT;

            default:
                return null;
        }
    }

    private boolean isParcelableAnnotationPresent(TypeMirror mirror) {

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

    private boolean isParcelableList(TypeMirror typeMirror) {

        if (!isList(typeMirror)) {
            return false;
        }

        final DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments == null
                || typeArguments.size() != 1) {
            return false;
        }

        final TypeMirror listType = typeArguments.get(0);

        return isSubtype(listType, ANDROID_OS_PARCELABLE) || isParcelableAnnotationPresent(listType);

    }

    private boolean isList(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }

        final TypeMirror erasedMirror = mTypes.erasure(typeMirror);
        final String erasure = erasedMirror.toString();

        return erasure.startsWith(JAVA_UTIL_LIST)
                || erasure.startsWith(JAVA_UTIL_ARRAY_LIST);
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

        return superMirror.toString().startsWith(JAVA_LANG_ENUM);
    }

    private boolean isSubtype(TypeMirror typeMirror, String toCheck) {
        final TypeElement typeElement = mElements.getTypeElement(toCheck);
        return mTypes.isAssignable(typeMirror, typeElement.asType());
    }

    private boolean shouldCallSuper(TypeElement typeElement) {
        final TypeMirror superClass = typeElement.getSuperclass();
        return isParcelableAnnotationPresent(superClass);
    }
}
