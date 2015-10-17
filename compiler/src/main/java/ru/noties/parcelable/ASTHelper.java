package ru.noties.parcelable;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
public class ASTHelper {

    private final TreeMaker mTreeMaker;
    private final Names mNames;

    ASTHelper(TreeMaker treeMaker, Names names) {
        this.mTreeMaker = treeMaker;
        this.mNames = names;
    }

    public TreeMaker getTreeMaker() {
        return mTreeMaker;
    }

    public JCTree.JCExpression getPrimitiveType(TypeKind kind) {

        final int tag;

        switch (kind) {

            case BYTE:
                tag = 1;
                break;

            case CHAR:
                tag = 2;
                break;

            case SHORT:
                tag = 3;
                break;

            case INT:
                tag = 4;
                break;

            case LONG:
                tag = 5;
                break;

            case FLOAT:
                tag = 6;
                break;

            case DOUBLE:
                tag = 7;
                break;

            case BOOLEAN:
                tag = 8;
                break;

            case VOID:
                tag = 9;
                break;

            default:
                throw new IllegalArgumentException("Supplied TypeKind is not a primitive: " + kind);
        }

        return mTreeMaker.TypeIdent(tag);
    }

    public JCTree.JCExpression getType(String pkg, String cl) {
        return mTreeMaker.Select(
                mTreeMaker.Ident(mNames.fromString(pkg)),
                mNames.fromString(cl)
        );
    }

    public JCTree.JCModifiers getModifiers(long... modifiers) {
        long val = 0L;
        if (modifiers != null
                && modifiers.length > 0) {
            for (long mod : modifiers) {
                val |= mod;
            }
        }
        return mTreeMaker.Modifiers(val);
    }

    public Name getName(String name) {
        return mNames.fromString(name);
    }

    public JCTree.JCExpression getSelection(String ident, String name) {
        return mTreeMaker.Select(mTreeMaker.Ident(getName(ident)), getName(name));
    }

    public void removeMethodIfExists(JCTree.JCClassDecl jcClassDecl, String methodName) {

        List<JCTree> defs = List.nil();
        boolean modified = false;
        JCTree.JCMethodDecl jcMethodDecl;

        for (JCTree tree : jcClassDecl.defs) {

            if (tree.getKind() == Tree.Kind.METHOD) {
                jcMethodDecl = (JCTree.JCMethodDecl) tree;
                if (methodName.equals(jcMethodDecl.getName().toString())) {
                    modified = true;
                    continue;
                }
            }

            defs = defs.append(tree);
        }

        if (modified) {
            jcClassDecl.defs = defs;
        }
    }

    public JCTree.JCExpression getNull() {
        return mTreeMaker.Literal(17, null);
    }

    public JCTree.JCExpression getEquals(JCTree.JCExpression left, JCTree.JCExpression right) {
        return mTreeMaker.Binary(62, left, right);
    }

    public JCTree.JCExpression getNotEquals(JCTree.JCExpression left, JCTree.JCExpression right) {
        return mTreeMaker.Binary(63, left, right);
    }

    public JCTree.JCExpression getLess(JCTree.JCExpression left, JCTree.JCExpression right) {
        return mTreeMaker.Binary(64, left, right);
    }

    public JCTree.JCExpression getGreater(JCTree.JCExpression left, JCTree.JCExpression right) {
        return mTreeMaker.Binary(65, left, right);
    }

    public JCTree.JCExpression getPlus(JCTree.JCExpression left, JCTree.JCExpression right) {
        return mTreeMaker.Binary(48, left, right);
    }

    public JCTree.JCExpression getSimpleMethodCall(String who, String method, JCTree.JCExpression... params) {
        return mTreeMaker.Apply(
                List.<JCTree.JCExpression>nil(),
                mTreeMaker.Select(mTreeMaker.Ident(getName(who)), getName(method)),
                params.length > 0 ? List.from(params) : List.<JCTree.JCExpression>nil()
        );
    }

    public Element findFieldByName(Element root, String name) {
        for (Element element: root.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }

            if (name.equals(element.getSimpleName().toString())) {
                return element;
            }
        }
        return null;
    }

    public JCTree.JCExpression getTypeFromElement(Element element, boolean isArray) {

        if (element == null) {
            return null;
        }

        final String typeString = element.asType().toString();
        final int lastIndex = typeString.lastIndexOf('.');
        if (lastIndex == -1) {
            return null;
        }

        final String packageName = typeString.substring(0, lastIndex);
        final int substringEnd = isArray ? typeString.length() - 2 : typeString.length();
        final String typeName = typeString.substring(lastIndex + 1, substringEnd);

        return getType(packageName, typeName);
    }
}
