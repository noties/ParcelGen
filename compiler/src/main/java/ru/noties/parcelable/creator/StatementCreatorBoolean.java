package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorBoolean extends StatementCreatorBase {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        if (isArray) {
            return super.createReadFromParcel(astHelper, rootElement, parcel, varName, true);
        }

        final TreeMaker treeMaker = astHelper.getTreeMaker();
        final JCTree.JCStatement booleanStatement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(astHelper.getName(varName)),
                        astHelper.getEquals(
                                treeMaker.Apply(
                                        List.<JCTree.JCExpression>nil(),
                                        treeMaker.Select(parcel, astHelper.getName("readByte")),
                                        List.<JCTree.JCExpression>nil()
                                ),
                                treeMaker.TypeCast(astHelper.getPrimitiveType(TypeKind.BYTE), treeMaker.Literal(1))
                        )
                )
        );
        return List.of(booleanStatement);
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {

        if (isArray) {
            return super.createWriteToParcel(astHelper, rootElement, parcel, flags, varName, true);
        }

        final TreeMaker treeMaker = astHelper.getTreeMaker();
        final JCTree.JCExpression booleanConditional = treeMaker.Conditional(
                treeMaker.Ident(astHelper.getName(varName)),
                treeMaker.TypeCast(astHelper.getPrimitiveType(TypeKind.BYTE), treeMaker.Literal(1)),
                treeMaker.TypeCast(astHelper.getPrimitiveType(TypeKind.BYTE), treeMaker.Literal(0))
        );

        final JCTree.JCStatement statement =  treeMaker.Exec(
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName("writeByte")),
                        List.of(booleanConditional)
                )
        );

        return List.of(statement);
    }

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return isArray ? "createBooleanArray" : null;
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeBooleanArray" : null;
    }
}
