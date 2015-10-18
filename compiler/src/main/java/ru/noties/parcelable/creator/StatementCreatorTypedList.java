package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorTypedList extends StatementCreatorBase {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        // parcel.createTypedArrayList(Type.CREATOR)

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        // here we have List<OUR_TYPE>
        final Element fieldElement = astHelper.findFieldByName(rootElement, varName);
        if (fieldElement == null) {
            return List.nil();
        }

        final JCTree.JCExpression typeOfElement = astHelper.getListTypeParameter((DeclaredType) fieldElement.asType());

        final JCTree.JCExpression creator = treeMaker.Select(typeOfElement, astHelper.getName("CREATOR"));

        final JCTree.JCStatement statement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(astHelper.getName(varName)),
                        treeMaker.Apply(
                                List.<JCTree.JCExpression>nil(),
                                treeMaker.Select(parcel, astHelper.getName("createTypedArrayList")),
                                List.of(creator)
                        )
                )
        );

        return List.of(statement);
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {
        return super.createWriteToParcel(astHelper, rootElement, parcel, flags, varName, isArray);
    }

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return null;
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? null : "writeTypedList";
    }
}
