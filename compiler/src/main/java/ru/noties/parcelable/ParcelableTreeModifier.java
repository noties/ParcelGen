package ru.noties.parcelable;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.lang.reflect.Modifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
class ParcelableTreeModifier {

    static ParcelableTreeModifier newInstance(ProcessingEnvironment environment, ParcelableLogger logger) {
        final Context context = ((JavacProcessingEnvironment) environment).getContext();
        return new ParcelableTreeModifier(
                logger,
                Trees.instance(environment),
                TreeMaker.instance(context),
                Names.instance(context),
                environment.getElementUtils()
        );
    }

    final ParcelableLogger mLogger;
    final Trees mTrees;
    final TreeMaker mTreeMaker;
    final Elements mElements;

    final ASTHelper mASTHelper;

    final JCTree.JCExpression mParcelableType;

    private ParcelableTreeModifier(ParcelableLogger logger, Trees trees, TreeMaker treeMaker, Names names, Elements elements) {
        this.mLogger = logger;
        this.mTrees = trees;
        this.mTreeMaker = treeMaker;
        this.mElements = elements;
        this.mASTHelper = new ASTHelper(mTreeMaker, names);
        this.mParcelableType = mASTHelper.getType("android.os", "Parcelable");
    }

    boolean modify(ParcelableData data) {

        final JCTree classTree = (JCTree) mTrees.getTree(data.element);
        classTree.accept(new ParcelableVisitor(data));

        return true;
    }

    private Element findFieldByName(Element root, String name) {
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

    private JCTree.JCExpression getTypeFromElement(Element element, boolean isArray) {

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

        return mASTHelper.getType(packageName, typeName);
    }

    private class ParcelableVisitor extends TreeTranslator {

        private final ParcelableData data;

        ParcelableVisitor(ParcelableData data) {
            this.data = data;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            super.visitClassDef(jcClassDecl);

            addImplements(jcClassDecl);
            addParcelableConstructor(jcClassDecl);
            addDescribeContentsMethod(jcClassDecl);
            addCreator(jcClassDecl);
            addWriteToParcelMethod(jcClassDecl);
        }

        private void addImplements(JCTree.JCClassDecl jcClassDecl) {

            final List<JCTree.JCExpression> currentImplements = jcClassDecl.getImplementsClause();
            // check if we already have Parcelable
            if (currentImplements != null
                    && currentImplements.size() > 0) {
                for (JCTree.JCExpression currImpl: currentImplements) {
                    if ("android.os.Parcelable".equalsIgnoreCase(currImpl.toString())) {
                        return;
                    }
                }
            }

            // create implements Parcelable statement
            final JCTree.JCExpression expression = mASTHelper.getType("android.os", "Parcelable");

            // append to current statements
            jcClassDecl.implementing = jcClassDecl.getImplementsClause().append(expression);
        }

        private void addParcelableConstructor(JCTree.JCClassDecl jcClassDecl) {

            final JCTree.JCMethodDecl methodDecl = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("<init>"),
                    null,
                    List.<JCTree.JCTypeParameter>nil(),
                    List.of(mTreeMaker.VarDef(mASTHelper.getModifiers(), mASTHelper.getName("source"), mASTHelper.getType("android.os", "Parcel"), null)),
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Block(0, createParcelConstructorCalls()),
                    null
            );
            jcClassDecl.defs = jcClassDecl.defs.append(methodDecl);
        }

        private List<JCTree.JCStatement> createParcelConstructorCalls() {

            List<JCTree.JCStatement> calls = List.nil();

            final JCTree.JCStatement callSuper = mTreeMaker.Exec(
                    mTreeMaker.Apply(
                            List.<JCTree.JCExpression>nil(),
                            mTreeMaker.Ident(mASTHelper.getName("super")),
                            List.<JCTree.JCExpression>nil()
                    )
            );

            calls = calls.append(callSuper);

            ParcelableType type;
            String callName;
            boolean isArray;

            for (ParcelableItem item: data.items) {

                callName = null;
                isArray = false;
                type = item.type;

                while (type != null) {

                    switch (type) {

                        case BYTE:
                            callName = isArray ? "createByteArray" : "readByte";
                            break;

                        case INT:
                            callName = isArray ? "createIntArray" : "readInt";
                            break;

                        case LONG:
                            callName = isArray ? "createLongArray" : "readLong";
                            break;

                        case FLOAT:
                            callName = isArray ? "createFloatArray" : "readFloat";
                            break;

                        case DOUBLE:
                            callName = isArray ? "createDoubleArray" : "readDouble";
                            break;

                        case STRING:
                            callName = isArray ? "createStringArray" : "readString";
                            break;

                        case ARRAY:
                            isArray = true;
                            type = ((ParcelableItemArray) item).arrayType;
                            continue;


                        case BOOLEAN:
                            if (isArray) {
                                callName = "createBooleanArray";
                            } else {
                                final JCTree.JCStatement booleanStatement = mTreeMaker.Exec(
                                        mTreeMaker.Assign(
                                                mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                                mASTHelper.getEquals(
                                                        mASTHelper.getSimpleMethodCall("source", "readByte"),
                                                        mTreeMaker.TypeCast(mASTHelper.getPrimitiveType(TypeKind.BYTE), mTreeMaker.Literal(1))
                                                )
                                        )
                                );
                                calls = calls.append(booleanStatement);
                            }
                            break;

                        case ENUM:

                            // int = read
                            // value = int > -1 ? Value.values()[int] : null

                            final JCTree.JCExpression enumTypeExpression = getTypeFromElement(findFieldByName(data.element, item.name), false);
                            if (enumTypeExpression == null) {
                                break;
                            }

                            final String tmpEnumName = "_" + item.name;
                            final JCTree.JCExpression tmpEnumIdent = mTreeMaker.Ident(mASTHelper.getName(tmpEnumName));

                            final JCTree.JCStatement enumVar = mTreeMaker.VarDef(
                                    mASTHelper.getModifiers(Modifier.FINAL),
                                    mASTHelper.getName(tmpEnumName),
                                    mASTHelper.getPrimitiveType(TypeKind.INT),
                                    mASTHelper.getSimpleMethodCall("source", "readInt")
                            );

                            final JCTree.JCExpression enumCond = mTreeMaker.Conditional(
                                    mASTHelper.getGreater(tmpEnumIdent, mTreeMaker.Literal(-1)),
                                    mTreeMaker.Indexed(
                                            mTreeMaker.Apply(
                                                    List.<JCTree.JCExpression>nil(),
                                                    mTreeMaker.Select(enumTypeExpression, mASTHelper.getName("values")),
                                                    List.<JCTree.JCExpression>nil()
                                            ),
                                            tmpEnumIdent
                                    ),
                                    mASTHelper.getNull()
                            );

                            final JCTree.JCStatement enumStatement = mTreeMaker.Exec(
                                    mTreeMaker.Assign(
                                            mTreeMaker.Ident(mASTHelper.getName(item.name)), enumCond
                                    )
                            );

                            calls = calls.append(enumVar).append(enumStatement);

                            break;

                        case SERIALIZABLE:
                            callName = "readSerializable";
                            break;

                        case PARCELABLE:

                            final Element element = findFieldByName(data.element, item.name);
                            final JCTree.JCExpression typeExpression = getTypeFromElement(element, isArray);

                            if (typeExpression == null) {
                                break;
                            }

                            final JCTree.JCExpression classLoader = mTreeMaker.Apply(
                                    List.<JCTree.JCExpression>nil(),
                                    mTreeMaker.Select(
                                            mTreeMaker.Select(
                                                    typeExpression,
                                                    mASTHelper.getName("class")
                                            ),
                                            mASTHelper.getName("getClassLoader")
                                    ),
                                    List.<JCTree.JCExpression>nil()
                            );

                            if (!isArray) {
                                calls = calls.append(
                                        mTreeMaker.Exec(
                                                mTreeMaker.Assign(
                                                        mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                                        mTreeMaker.TypeCast(
                                                                typeExpression,
                                                                mASTHelper.getSimpleMethodCall("source", "readParcelable", classLoader)
                                                        )
                                                )
                                        )
                                );
                                break;
                            }



                            // create a variable Parcelable[] p = source.readParcelableArray(classloader)
                            // create a variable of out type[]
                            // for (int i = 0; i < p.lenght; i++ {
                            // out[i] = (Type) p[i]
                            // }
                            // assign

                            final String tmpParArrName = "_" + item.name;

                            final JCTree.JCStatement parcelableArray = mTreeMaker.VarDef(
                                    mASTHelper.getModifiers(Modifier.FINAL),
                                    mASTHelper.getName(tmpParArrName),
                                    mTreeMaker.TypeArray(mASTHelper.getType("android.os", "Parcelable")),
                                    mASTHelper.getSimpleMethodCall("source", "readParcelableArray", classLoader)
                            );

                            final JCTree.JCStatement outArray = mTreeMaker.VarDef(
                                    mASTHelper.getModifiers(Modifier.FINAL),
                                    mASTHelper.getName("oa"),
                                    mTreeMaker.TypeArray(typeExpression),
                                    mTreeMaker.NewArray(
                                            typeExpression,
                                            List.of(mASTHelper.getSelection(tmpParArrName, "length")),
                                            null
                                    )
                            );

                            final JCTree.JCExpression forIndex = mTreeMaker.Ident(mASTHelper.getName("i"));

                            final JCTree.JCStatement forInit = mTreeMaker.VarDef(
                                    mASTHelper.getModifiers(),
                                    mASTHelper.getName("i"),
                                    mASTHelper.getPrimitiveType(TypeKind.INT),
                                    mTreeMaker.Literal(0)
                            );

                            final JCTree.JCStatement forBodyLine = mTreeMaker.Exec(
                                    mTreeMaker.Assign(
                                            mTreeMaker.Indexed(mTreeMaker.Ident(mASTHelper.getName("oa")), forIndex),
                                            mTreeMaker.TypeCast(typeExpression, mTreeMaker.Indexed(mTreeMaker.Ident(mASTHelper.getName(tmpParArrName)), forIndex))
                                    )
                            );

                            final JCTree.JCStatement forLoop = mTreeMaker.ForLoop(
                                    List.of(forInit),
                                    mASTHelper.getLess(forIndex, mASTHelper.getSelection(tmpParArrName, "length")),
                                    List.of(mTreeMaker.Exec(
                                            mTreeMaker.Assign(forIndex, mASTHelper.getPlus(forIndex, mTreeMaker.Literal(1)))
                                    )),
                                    forBodyLine
                            );

                            final JCTree.JCStatement assignNotNull = mTreeMaker.Exec(
                                    mTreeMaker.Assign(
                                            mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                            mTreeMaker.Ident(mASTHelper.getName("oa"))
                                    )
                            );

                            final JCTree.JCStatement assignNull = mTreeMaker.Exec(
                                    mTreeMaker.Assign(
                                            mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                            mASTHelper.getNull()
                                    )
                            );

                            final JCTree.JCBlock ifNotNullBlock = mTreeMaker.Block(
                                    0L,
                                    List.of(outArray, forLoop, assignNotNull)
                            );

                            final JCTree.JCBlock ifNullBlock = mTreeMaker.Block(
                                    0L,
                                    List.of(assignNull)
                            );

                            final JCTree.JCStatement ifStatement = mTreeMaker.If(
                                    mASTHelper.getNotEquals(mTreeMaker.Ident(mASTHelper.getName(tmpParArrName)), mASTHelper.getNull()),
                                    ifNotNullBlock,
                                    ifNullBlock
                            );

                            calls = calls.append(parcelableArray).append(ifStatement);

                            break;
                        default:
                            break;
                    }

                    type = null;
                }

                if (callName == null) {
                    continue;
                }

                calls = calls.append(
                    mTreeMaker.Exec(
                            mTreeMaker.Assign(
                                    mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                    mASTHelper.getSimpleMethodCall("source", callName)
                            )
                    )
                );
            }

            return calls;
        }

        private void addDescribeContentsMethod(JCTree.JCClassDecl jcClassDecl) {

            // check if this method already exists
            mASTHelper.removeMethodIfExists(jcClassDecl, "describeContents");

            // create describeContents method
            final JCTree.JCExpression describeContentsExpression = mTreeMaker.Literal(0);
            final JCTree.JCStatement describeContentsStatement = mTreeMaker.Return(describeContentsExpression);
            final JCTree.JCBlock describeContentsBlock = mTreeMaker.Block(0L, List.of(describeContentsStatement));

            final JCTree.JCExpression returnType = mASTHelper.getPrimitiveType(TypeKind.INT);

            final JCTree.JCMethodDecl describeContentsMethod = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC), // method modifiers (1L = public)
                    mASTHelper.getName("describeContents"), // method name
                    returnType, // method return type
                    List.<JCTree.JCTypeParameter>nil(),
                    List.<JCTree.JCVariableDecl>nil(),
                    List.<JCTree.JCExpression>nil(),
                    describeContentsBlock,
                    null
            );

            jcClassDecl.defs = jcClassDecl.getMembers().append(describeContentsMethod);
        }

        private void addCreator(JCTree.JCClassDecl jcClassDecl) {

            final JCTree.JCModifiers modifiers = mASTHelper.getModifiers(
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
            );

            final JCTree.JCExpression type = mTreeMaker.Select(
                    mASTHelper.getType("android.os", "Parcelable"),
                    mASTHelper.getName("Creator")
            );

            final JCTree.JCStatement returnInstanceStatement = mTreeMaker.Return(
                    mTreeMaker.NewClass(
                            null,
                            null,
                            mTreeMaker.Ident(jcClassDecl.name),
                            List.<JCTree.JCExpression>of(mTreeMaker.Ident(mASTHelper.getName("dest"))),
                            null
                    )
            );

            JCTree.JCMethodDecl createFromParcel = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("createFromParcel"),
                    mTreeMaker.Ident(jcClassDecl.name),
                    List.<JCTree.JCTypeParameter>nil(),
                    List.of(mTreeMaker.VarDef(mTreeMaker.Modifiers(0L), mASTHelper.getName("dest"), mASTHelper.getType("android.os", "Parcel"), null)),
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Block(0L, List.of(returnInstanceStatement)),
                    null
            );

            final JCTree.JCStatement returnArrayStatement = mTreeMaker.Return(
                    mTreeMaker.NewArray(
                            mTreeMaker.Ident(jcClassDecl.name),
                            List.<JCTree.JCExpression>of(mTreeMaker.Ident(mASTHelper.getName("size"))),
                            null
                    )
            );

            final JCTree.JCMethodDecl newArray = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("newArray"),
                    mTreeMaker.TypeArray(mTreeMaker.Ident(jcClassDecl.name)),
                    List.<JCTree.JCTypeParameter>nil(), // generic type parameters
                    List.of((mTreeMaker.VarDef(mASTHelper.getModifiers(), mASTHelper.getName("size"), mASTHelper.getPrimitiveType(TypeKind.INT), null))),
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Block(0L, List.of(returnArrayStatement)),
                    null
            );

            final JCTree.JCClassDecl initClassBody = mTreeMaker.AnonymousClassDef(
                    mASTHelper.getModifiers(),
                    List.of((JCTree) createFromParcel, newArray)
            );

            JCTree.JCExpression init = mTreeMaker.NewClass(
                    null,
                    null,
                    type,
                    List.<JCTree.JCExpression>nil(),
                    initClassBody
            );

            final JCTree.JCVariableDecl creatorVar = mTreeMaker.VarDef(
                    modifiers,
                    mASTHelper.getName("CREATOR"),
                    type,
                    init
            );

            jcClassDecl.defs = jcClassDecl.getMembers().append(creatorVar);
        }

        private void addWriteToParcelMethod(JCTree.JCClassDecl jcClassDecl) {

            // check if we already have a `writeToParcel` method & remove it if exists
            mASTHelper.removeMethodIfExists(jcClassDecl, "writeToParcel");

            final JCTree.JCVariableDecl varDest = mTreeMaker.VarDef(
                    mASTHelper.getModifiers(),
                    mASTHelper.getName("dest"),
                    mASTHelper.getType("android.os", "Parcel"),
                    null
            );

            final JCTree.JCVariableDecl varFlags = mTreeMaker.VarDef(
                    mASTHelper.getModifiers(),
                    mASTHelper.getName("flags"),
                    mASTHelper.getPrimitiveType(TypeKind.INT),
                    null
            );

            final JCTree.JCMethodDecl writeToParcelMethod = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("writeToParcel"),
                    mASTHelper.getPrimitiveType(TypeKind.VOID),
                    List.<JCTree.JCTypeParameter>nil(),
                    List.<JCTree.JCVariableDecl>of(varDest, varFlags),
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Block(0L, createWriteToParcelCalls()),
                    null
            );

            jcClassDecl.defs = jcClassDecl.defs.append(writeToParcelMethod);
        }

        private List<JCTree.JCStatement> createWriteToParcelCalls() {

            final JCTree.JCExpression dest = mTreeMaker.Ident(mASTHelper.getName("dest"));

            List<JCTree.JCStatement> calls = List.nil();

            String callName;
            List<JCTree.JCExpression> callExp;

            for (ParcelableItem item: data.items) {

                callName = null;
                callExp = null;

                boolean isArray = false;
                ParcelableType type = item.type;

                while (type != null) {

                    switch (type) {

                        case BYTE:
                            callName = isArray ? "writeByteArray" : "writeByte";
                            break;

                        case INT:
                            callName = isArray ? "writeIntArray" : "writeInt";
                            break;

                        case LONG:
                            callName = isArray ? "writeLongArray" : "writeLong";
                            break;

                        case FLOAT:
                            callName = isArray ? "writeFloatArray" : "writeFloat";
                            break;

                        case DOUBLE:
                            callName = isArray ? "writeDoubleArray" : "writeDouble";
                            break;

                        case STRING:
                            callName = isArray ? "writeStringArray" : "writeString";
                            break;

                        case BOOLEAN:
                            callName = isArray ? "writeBooleanArray" : null;
                            if (!isArray) {

                                final JCTree.JCExpression booleanConditional = mTreeMaker.Conditional(
                                        mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                        mTreeMaker.TypeCast(mASTHelper.getPrimitiveType(TypeKind.BYTE), mTreeMaker.Literal(1)),
                                        mTreeMaker.TypeCast(mASTHelper.getPrimitiveType(TypeKind.BYTE), mTreeMaker.Literal(0))
                                );

                                calls = calls.append(
                                        mTreeMaker.Exec(
                                                mASTHelper.getSimpleMethodCall("dest", "writeByte", booleanConditional)
                                        )
                                );
                            }
                            break;

                        case SERIALIZABLE:
                            callName = "writeSerializable";
                            break;

                        case PARCELABLE:
                            callName = isArray ? "writeParcelableArray" : "writeParcelable";
                            callExp = List.of(
                                    (JCTree.JCExpression) mTreeMaker.Ident(mASTHelper.getName(item.name)),
                                    mTreeMaker.Ident(mASTHelper.getName("flags"))
                            );
                            break;

                        case ENUM:
                            callName = isArray ? "writeInt" : null;

                            final JCTree.JCExpression expression = mTreeMaker.Conditional(
                                    mASTHelper.getNotEquals(mTreeMaker.Ident(mASTHelper.getName(item.name)), mASTHelper.getNull()),
                                    mASTHelper.getSimpleMethodCall(item.name, "ordinal"),
                                    mTreeMaker.Literal(-1)
                            );

                            calls = calls.append(
                                    mTreeMaker.Exec(
                                        mASTHelper.getSimpleMethodCall("dest", "writeInt", expression)
                                    )
                            );

                            break;

                        case ARRAY:
                            isArray = true;
                            type = ((ParcelableItemArray) item).arrayType;
                            continue;
                    }

                    type = null;
                }

                if (callExp == null) {
                    callExp = List.of((JCTree.JCExpression) mTreeMaker.Ident(mASTHelper.getName(item.name)));
                }

                if (callName != null) {
                    calls = calls.append(
                            mTreeMaker.Exec(
                                    mTreeMaker.Apply(
                                            List.<JCTree.JCExpression>nil(),
                                            mTreeMaker.Select(dest, mASTHelper.getName(callName)),
                                            callExp
                                    )
                            )
                    );
                }
            }

            return calls;
        }
    }

    private static class ASTHelper {

        private final TreeMaker mTreeMaker;
        private final Names mNames;

        ASTHelper(TreeMaker treeMaker, Names names) {
            this.mTreeMaker = treeMaker;
            this.mNames = names;
        }

        JCTree.JCExpression getPrimitiveType(TypeKind kind) {

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

        JCTree.JCExpression getType(String pkg, String cl) {
            return mTreeMaker.Select(
                    mTreeMaker.Ident(mNames.fromString(pkg)),
                    mNames.fromString(cl)
            );
        }

        JCTree.JCModifiers getModifiers(long... modifiers) {
            long val = 0L;
            if (modifiers != null
                    && modifiers.length > 0) {
                for (long mod: modifiers) {
                    val |= mod;
                }
            }
            return mTreeMaker.Modifiers(val);
        }

        Name getName(String name) {
            return mNames.fromString(name);
        }

        JCTree.JCExpression getSelection(String ident, String name) {
            return mTreeMaker.Select(mTreeMaker.Ident(getName(ident)), getName(name));
        }

        void removeMethodIfExists(JCTree.JCClassDecl jcClassDecl, String methodName) {

            List<JCTree> defs = List.nil();
            boolean modified = false;
            JCTree.JCMethodDecl jcMethodDecl;

            for (JCTree tree: jcClassDecl.defs) {

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

        JCTree.JCExpression getNull() {
            return mTreeMaker.Literal(17, null);
        }

        JCTree.JCExpression getEquals(JCTree.JCExpression left, JCTree.JCExpression right) {
            return mTreeMaker.Binary(62, left, right);
        }

        JCTree.JCExpression getNotEquals(JCTree.JCExpression left, JCTree.JCExpression right) {
            return mTreeMaker.Binary(63, left, right);
        }

        JCTree.JCExpression getLess(JCTree.JCExpression left, JCTree.JCExpression right) {
            return mTreeMaker.Binary(64, left, right);
        }

        JCTree.JCExpression getGreater(JCTree.JCExpression left, JCTree.JCExpression right) {
            return mTreeMaker.Binary(65, left, right);
        }

        JCTree.JCExpression getPlus(JCTree.JCExpression left, JCTree.JCExpression right) {
            return mTreeMaker.Binary(48, left, right);
        }

        JCTree.JCExpression getSimpleMethodCall(String who, String method, JCTree.JCExpression... params) {
            return mTreeMaker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Select(mTreeMaker.Ident(getName(who)), getName(method)),
                    params.length > 0 ? List.from(params) : List.<JCTree.JCExpression>nil()
            );
        }
    }
}
