package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 18.10.2015.
 */
class StatementCreatorObject implements StatementCreator {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final Element element = astHelper.findFieldByName(rootElement, varName);
        if (element == null) {
            return List.nil();
        }

//        final JCTree.JCExpression type = astHelper.getTypeFromElement(element, isArray, false);
        final JCTree.JCExpression typeRaw = astHelper.getTypeFromElement(element, isArray, true);
        final JCTree.JCExpression classLoader = astHelper.getClassLoaderExpression(typeRaw);

        if (!isArray) {
            final JCTree.JCStatement statement = treeMaker.Exec(
                    treeMaker.Assign(
                            treeMaker.Ident(astHelper.getName(varName)),
                            treeMaker.TypeCast(
                                    typeRaw,
                                    treeMaker.Apply(
                                            List.<JCTree.JCExpression>nil(),
                                            treeMaker.Select(parcel, astHelper.getName("readValue")),
                                            List.of(classLoader)
                                    )
                            )
                    )
            );
            return List.of(statement);
        }



        return List.nil();
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        if (!isArray) {
            final JCTree.JCStatement statement = treeMaker.Exec(
                    treeMaker.Apply(
                            List.<JCTree.JCExpression>nil(),
                            treeMaker.Select(parcel, astHelper.getName("writeValue")),
                            List.of((JCTree.JCExpression) treeMaker.Ident(astHelper.getName(varName)))
                    )
            );
            return List.of(statement);
        }

        return List.nil();
    }
}
