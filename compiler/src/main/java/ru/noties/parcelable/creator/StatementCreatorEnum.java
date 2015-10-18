package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Modifier;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorEnum implements StatementCreator {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        if (isArray) {
            return List.nil();
        }

        // int = read
        // value = int > -1 ? Value.values()[int] : null

        final JCTree.JCExpression enumTypeExpression = astHelper.getTypeFromElement(
                astHelper.findFieldByName(rootElement, varName),
                false,
                false
        );

        if (enumTypeExpression == null) {
            return List.nil();
        }

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final String tmpEnumName = "_" + varName;
        final JCTree.JCExpression tmpEnumIdent = treeMaker.Ident(astHelper.getName(tmpEnumName));

        final JCTree.JCStatement enumVar = treeMaker.VarDef(
                astHelper.getModifiers(Modifier.FINAL),
                astHelper.getName(tmpEnumName),
                astHelper.getPrimitiveType(TypeKind.INT),
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName("readInt")),
                        List.<JCTree.JCExpression>nil()
                )
        );

        final JCTree.JCExpression enumCond = treeMaker.Conditional(
                astHelper.getGreater(tmpEnumIdent, treeMaker.Literal(-1)),
                treeMaker.Indexed(
                        treeMaker.Apply(
                                List.<JCTree.JCExpression>nil(),
                                treeMaker.Select(enumTypeExpression, astHelper.getName("values")),
                                List.<JCTree.JCExpression>nil()
                        ),
                        tmpEnumIdent
                ),
                astHelper.getNull()
        );

        final JCTree.JCStatement enumStatement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(astHelper.getName(varName)), enumCond
                )
        );

        return List.of(enumVar, enumStatement);
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {

        if (isArray) {
            return List.nil();
        }

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final JCTree.JCExpression expression = treeMaker.Conditional(
                astHelper.getNotEquals(treeMaker.Ident(astHelper.getName(varName)), astHelper.getNull()),
                astHelper.getSimpleMethodCall(varName, "ordinal"),
                treeMaker.Literal(-1)
        );

        final JCTree.JCStatement statement = treeMaker.Exec(
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName("writeInt")),
                        List.of(expression)
                )
        );

        return List.of(statement);
    }
}
