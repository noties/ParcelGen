package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import java.lang.reflect.Modifier;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
class StatementCreatorCharSequence implements StatementCreator {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final JCTree.JCExpression readCharSequence = treeMaker.Apply(
                List.<JCTree.JCExpression>nil(),
                treeMaker.Select(
                        treeMaker.Select(astHelper.getType("android.text", "TextUtils"), astHelper.getName("CHAR_SEQUENCE_CREATOR")),
                        astHelper.getName("createFromParcel")
                ),
                List.of(parcel)
        );

        final JCTree.JCExpression itemExpression = treeMaker.Ident(astHelper.getName(varName));

        if (!isArray) {
            final JCTree.JCStatement readCharSequenceStatement = treeMaker.Exec(
                    treeMaker.Assign(
                            itemExpression,
                            readCharSequence
                    )
            );
            return List.of(readCharSequenceStatement);
        }

        // final int charSequenceArrayLength = source.readInt();
        // if (csa == -1) { csa = null; }
        // else {
        //    this.csa = new CharSequence[csal];
        //    for (int i = 0; i < csal; i++) {
        //        this.csa[i] = TextUtils.CHAR.createFromParcel();
        //    }
        //}

        final Name lengthName = astHelper.getName("_" + varName + "length");

        final JCTree.JCVariableDecl length = treeMaker.VarDef(
                astHelper.getModifiers(Modifier.FINAL),
                lengthName,
                astHelper.getPrimitiveType(TypeKind.INT),
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName("readInt")),
                        List.<JCTree.JCExpression>nil()
                )
        );

        final JCTree.JCStatement minusBody = treeMaker.Exec(
                treeMaker.Assign(
                        itemExpression,
                        astHelper.getNull()
                )
        );

        final JCTree.JCStatement newCharSequenceArray = treeMaker.Exec(
                treeMaker.Assign(
                        itemExpression,
                        treeMaker.NewArray(
                                astHelper.getType("java.lang", "CharSequence"),
                                List.of((JCTree.JCExpression) treeMaker.Ident(lengthName)),
                                null
                        )
                )
        );

        final Name indexVarName = astHelper.getName("i");

        final JCTree.JCStatement indexVar = treeMaker.VarDef(
                astHelper.getModifiers(),
                indexVarName,
                astHelper.getPrimitiveType(TypeKind.INT),
                treeMaker.Literal(0)
        );

        final JCTree.JCExpressionStatement indexIncrement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(indexVarName),
                        astHelper.getPlus(
                                treeMaker.Ident(indexVarName),
                                treeMaker.Literal(1)
                        )
                )
        );

        final JCTree.JCStatement forLoopBody = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Indexed(itemExpression, treeMaker.Ident(indexVarName)),
                        readCharSequence
                )
        );

        final JCTree.JCStatement arrayForLoop = treeMaker.ForLoop(
                List.of(indexVar),
                astHelper.getLess(
                        treeMaker.Ident(astHelper.getName("i")),
                        treeMaker.Ident(lengthName)
                ),
                List.of(indexIncrement),
                forLoopBody
        );

        final JCTree.JCStatement notMinusBody = treeMaker.Block(
                0,
                List.of(newCharSequenceArray, arrayForLoop)
        );

        final JCTree.JCStatement ifMinus = treeMaker.If(
                astHelper.getEquals(treeMaker.Ident(lengthName), treeMaker.Literal(-1)),
                minusBody,
                notMinusBody
        );

        return List.of(length, ifMinus);
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {
        return List.nil();
    }
}
