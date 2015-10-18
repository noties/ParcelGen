package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 18.10.2015.
 */
public class StatementCreatorObject implements StatementCreator {

//    @Override
//    protected String getReadFromParcelMethodCallName(boolean isArray) {
//        return isArray ? "readArray" : "readValue";
//    }
//
//    @Override
//    protected String getWriteToParcelMethodCallName(boolean isArray) {
//        return isArray ? "writeArray" : "writeValue";
//    }
//
//    @Override
//    protected List<JCTree.JCExpression> getReadMethodCallParameters(ASTHelper astHelper, String varName, boolean isArray) {
//        return List.of(astHelper.getNull());
//    }

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {
        return List.nil();
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {
        return List.nil();
    }
}
