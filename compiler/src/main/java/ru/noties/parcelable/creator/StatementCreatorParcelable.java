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
class StatementCreatorParcelable extends StatementCreatorBase {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {

        final Element element = astHelper.findFieldByName(rootElement, varName);
        final JCTree.JCExpression typeExpression = astHelper.getTypeFromElement(element, isArray);

        if (typeExpression == null) {
            return List.nil();
        }

        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final JCTree.JCExpression classLoader = treeMaker.Apply(
                List.<JCTree.JCExpression>nil(),
                treeMaker.Select(
                        treeMaker.Select(
                                typeExpression,
                                astHelper.getName("class")
                        ),
                        astHelper.getName("getClassLoader")
                ),
                List.<JCTree.JCExpression>nil()
        );

        if (!isArray) {

            final JCTree.JCStatement statement = treeMaker.Exec(
                    treeMaker.Assign(
                            treeMaker.Ident(astHelper.getName(varName)),
                            treeMaker.TypeCast(
                                    typeExpression,
                                    treeMaker.Apply(
                                            List.<JCTree.JCExpression>nil(),
                                            treeMaker.Select(parcel, astHelper.getName("readParcelable")),
                                            List.of(classLoader)
                                    )
                            )
                    )
            );

            return List.of(statement);
        }



        // create a variable Parcelable[] p = source.readParcelableArray(classloader)
        // create a variable of out type[]
        // for (int i = 0; i < p.lenght; i++ {
        // out[i] = (Type) p[i]
        // }
        // assign

        final String tmpParArrName = "_" + varName;

        final JCTree.JCStatement parcelableArray = treeMaker.VarDef(
                astHelper.getModifiers(Modifier.FINAL),
                astHelper.getName(tmpParArrName),
                treeMaker.TypeArray(astHelper.getType("android.os", "Parcelable")),
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel,astHelper.getName("readParcelableArray")),
                        List.of(classLoader)
                )
        );

        final JCTree.JCStatement outArray = treeMaker.VarDef(
                astHelper.getModifiers(Modifier.FINAL),
                astHelper.getName("oa"),
                treeMaker.TypeArray(typeExpression),
                treeMaker.NewArray(
                        typeExpression,
                        List.of(astHelper.getSelection(tmpParArrName, "length")),
                        null
                )
        );

        final JCTree.JCExpression forIndex = treeMaker.Ident(astHelper.getName("i"));

        final JCTree.JCStatement forInit = treeMaker.VarDef(
                astHelper.getModifiers(),
                astHelper.getName("i"),
                astHelper.getPrimitiveType(TypeKind.INT),
                treeMaker.Literal(0)
        );

        final JCTree.JCStatement forBodyLine = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Indexed(treeMaker.Ident(astHelper.getName("oa")), forIndex),
                        treeMaker.TypeCast(typeExpression, treeMaker.Indexed(treeMaker.Ident(astHelper.getName(tmpParArrName)), forIndex))
                )
        );

        final JCTree.JCStatement forLoop = treeMaker.ForLoop(
                List.of(forInit),
                astHelper.getLess(forIndex, astHelper.getSelection(tmpParArrName, "length")),
                List.of(treeMaker.Exec(
                        treeMaker.Assign(forIndex, astHelper.getPlus(forIndex, treeMaker.Literal(1)))
                )),
                forBodyLine
        );

        final JCTree.JCStatement assignNotNull = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(astHelper.getName(varName)),
                        treeMaker.Ident(astHelper.getName("oa"))
                )
        );

        final JCTree.JCStatement assignNull = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Ident(astHelper.getName(varName)),
                        astHelper.getNull()
                )
        );

        final JCTree.JCBlock ifNotNullBlock = treeMaker.Block(
                0L,
                List.of(outArray, forLoop, assignNotNull)
        );

        final JCTree.JCBlock ifNullBlock = treeMaker.Block(
                0L,
                List.of(assignNull)
        );

        final JCTree.JCStatement ifStatement = treeMaker.If(
                astHelper.getNotEquals(treeMaker.Ident(astHelper.getName(tmpParArrName)), astHelper.getNull()),
                ifNotNullBlock,
                ifNullBlock
        );

        return List.of(parcelableArray, ifStatement);
    }

    @Override
    protected String getReadFromParcelMethodCallName(boolean isArray) {
        return null;
    }

    @Override
    protected String getWriteToParcelMethodCallName(boolean isArray) {
        return isArray ? "writeParcelableArray" : "writeParcelable";
    }

    @Override
    protected List<JCTree.JCExpression> getWriteMethodCallParameters(TreeMaker treeMaker, Name varName, JCTree.JCExpression flags, boolean isArray) {
        return super.getWriteMethodCallParameters(treeMaker, varName, flags, isArray).append(flags);
    }
}
