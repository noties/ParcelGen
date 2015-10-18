package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 18.10.2015.
 */
class StatementCreatorList extends StatementCreatorBase {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        // var = new ArrayList
        // parcel.readList(var, ClassLoader)

        final TreeMaker treeMaker = astHelper.getTreeMaker();
        final Element fieldElement = astHelper.findFieldByName(rootElement, varName);

        if (fieldElement == null) {
            return List.nil();
        }

        final JCTree.JCExpression type = astHelper.getListTypeParameter((DeclaredType) fieldElement.asType());

        final JCTree.JCExpression create = treeMaker.Assign(
                treeMaker.Ident(astHelper.getName(varName)),
                treeMaker.NewClass(
                        null,
                        null,
                        treeMaker.TypeApply(astHelper.getType("java.util", "ArrayList"), List.of(type)), // our ArrayList<TYPE>
                        List.<JCTree.JCExpression>nil(), // params
                        null
                )
        );

        final JCTree.JCExpression classLoader = astHelper.getClassLoaderExpression(type);

        final JCTree.JCStatement call = treeMaker.Exec(
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName("readList")),
                        List.of(
                                treeMaker.Ident(astHelper.getName(varName)),
                                classLoader
                        )
                )
        );

        return List.of(
                treeMaker.Exec(create),
                call
        );
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
        return isArray ? null : "writeList";
    }
}
