package ru.noties.parcelable.creator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.Element;

import ru.noties.parcelable.ASTHelper;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
abstract class StatementCreatorBase implements StatementCreator {

    @Override
    public List<JCTree.JCStatement> createReadFromParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, String varName, boolean isArray) {
        final TreeMaker treeMaker = astHelper.getTreeMaker();
        final JCTree.JCExpression expression = treeMaker.Assign(
                treeMaker.Ident(astHelper.getName(varName)),
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(parcel, astHelper.getName(getReadFromParcelMethodCallName(isArray))),
                        List.<JCTree.JCExpression>nil()
                )
        );
        return List.of((JCTree.JCStatement)treeMaker.Exec(expression));
    }

    @Override
    public List<JCTree.JCStatement> createWriteToParcel(ASTHelper astHelper, Element rootElement, JCTree.JCExpression parcel, JCTree.JCExpression flags, String varName, boolean isArray) {
        final TreeMaker treeMaker = astHelper.getTreeMaker();
        final JCTree.JCExpression expression = treeMaker.Apply(
                List.<JCTree.JCExpression>nil(),
                treeMaker.Select(parcel, astHelper.getName(getWriteToParcelMethodCallName(isArray))),
                getWriteMethodCallParameters(treeMaker, astHelper.getName(varName), flags, isArray)
        );
        return List.of((JCTree.JCStatement) treeMaker.Exec(expression));
    }

    protected abstract String getReadFromParcelMethodCallName(boolean isArray);
    protected abstract String getWriteToParcelMethodCallName(boolean isArray);

    protected List<JCTree.JCExpression> getWriteMethodCallParameters(TreeMaker treeMaker, Name varName, JCTree.JCExpression flags, boolean isArray) {
        return List.of((JCTree.JCExpression) treeMaker.Ident(varName));
    }
}
