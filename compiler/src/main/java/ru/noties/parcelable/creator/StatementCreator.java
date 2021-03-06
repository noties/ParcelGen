package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
public interface StatementCreator {

    List<JCTree.JCStatement> createReadFromParcel(
            ASTHelper astHelper,
            Element rootElement,
            JCTree.JCExpression parcel,
            String varName,
            boolean isArray
    );

    List<JCTree.JCStatement> createWriteToParcel(
            ASTHelper astHelper,
            Element rootElement,
            JCTree.JCExpression parcel,
            JCTree.JCExpression flags,
            String varName,
            boolean isArray
    );
}
