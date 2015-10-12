package ru.noties.parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
public class ParcelableProcessor extends AbstractProcessor implements ParcelableLogger {

    static final String ANNOTATION = "ru.noties.parcelable.ParcelGen";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ANNOTATION);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private Types mTypes;
    private Elements mElements;
    private Messager mMessager;

    private ParcelableTreeModifier mTreeModifier;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mTypes = processingEnv.getTypeUtils();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();

        mTreeModifier = ParcelableTreeModifier.newInstance(processingEnv, this);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {

            boolean result = false;

            for (TypeElement typeElement: annotations) {
                result |= processElements(roundEnv.getElementsAnnotatedWith(typeElement));
            }

            return result;

        } catch (Throwable t) {
            log(Diagnostic.Kind.ERROR, "Exception during processing, throwable: %s", t);
        }

        return false;
    }

    private boolean processElements(Set<? extends Element> elements) throws Throwable {

        if (elements == null
                || elements.size() == 0) {
            return false;
        }

        final ParcelableDataParser parser = new ParcelableDataParser(this, mTypes, mElements);
        final List<ParcelableData> datas = new ArrayList<>();

        ParcelableData data;

        for (Element element: elements) {
            data = parser.parse(element);
            if (data != null) {
                datas.add(data);
            }
        }

        if (datas.size() == 0) {
            return false;
        }

        boolean result = false;

        for (ParcelableData parcelableData: datas) {
            result |= mTreeModifier.modify(parcelableData);
        }

        return result;
    }

    @Override
    public void log(Diagnostic.Kind level, String message, Object... args) {
        final String out;
        if (args == null
                || args.length == 0) {
            out = message;
        } else {
            out = String.format(message, args);
        }
        mMessager.printMessage(level, out);
    }
}
