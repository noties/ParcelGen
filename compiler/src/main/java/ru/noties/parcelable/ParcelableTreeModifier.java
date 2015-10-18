package ru.noties.parcelable;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import java.lang.reflect.Modifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import ru.noties.parcelable.creator.StatementCreator;
import ru.noties.parcelable.creator.StatementCreatorFactory;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
class ParcelableTreeModifier {

    static final String ANDROID_OS_PARCELABLE = "android.os.Parcelable";
    static final String READ_PARCEL_VAR_NAME = "source";
    static final String WRITE_PARCEL_VAR_NAME = "dest";
    static final String WRITE_FLAGS_VAR_NAME = "flags";

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
    final JCTree.JCExpression mParcelType;

    private ParcelableTreeModifier(ParcelableLogger logger, Trees trees, TreeMaker treeMaker, Names names, Elements elements) {
        this.mLogger = logger;
        this.mTrees = trees;
        this.mTreeMaker = treeMaker;
        this.mElements = elements;
        this.mASTHelper = new ASTHelper(mTreeMaker, names);

        this.mParcelableType    = mASTHelper.getType("android.os", "Parcelable");
        this.mParcelType        = mASTHelper.getType("android.os", "Parcel");
    }

    boolean modify(ParcelableData data) {

        final Statements statements = buildStatements(mASTHelper, data);

        final JCTree classTree = (JCTree) mTrees.getTree(data.element);
        classTree.accept(new ParcelableVisitor(statements, data.shouldCallSuper));

        return true;
    }

    private class ParcelableVisitor extends TreeTranslator {

        private final Statements mStatements;
        private final boolean mCallSuper;

        ParcelableVisitor(Statements statements, boolean callSuper) {
            this.mStatements = statements;
            this.mCallSuper = callSuper;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            super.visitClassDef(jcClassDecl);

            addImplements(jcClassDecl);
            ensureEmptyConstructor(jcClassDecl);
            addParcelableConstructor(jcClassDecl);
            addDescribeContentsMethod(jcClassDecl);
            addCreator(jcClassDecl);
            addWriteToParcelMethod(jcClassDecl);

//            mLogger.log(Diagnostic.Kind.NOTE, "class; %s", jcClassDecl);
        }

        private void addImplements(JCTree.JCClassDecl jcClassDecl) {

            final List<JCTree.JCExpression> currentImplements = jcClassDecl.getImplementsClause();
            // check if we already have Parcelable
            if (currentImplements != null
                    && currentImplements.size() > 0) {
                for (JCTree.JCExpression currImpl: currentImplements) {
                    if (ANDROID_OS_PARCELABLE.equalsIgnoreCase(currImpl.toString())) {
                        return;
                    }
                }
            }

            // append to current statements
            jcClassDecl.implementing = jcClassDecl.getImplementsClause().append(mParcelableType);
        }

        private void ensureEmptyConstructor(JCTree.JCClassDecl jcClassDecl) {

            JCTree.JCMethodDecl methodDecl;
            List<JCTree.JCVariableDecl> params;

            boolean isEmptyConstructorPresent = false;

            for (JCTree member: jcClassDecl.defs) {

                if (member.getKind() != Tree.Kind.METHOD) {
                    continue;
                }

                methodDecl = (JCTree.JCMethodDecl) member;
                if ("<init>".equals(methodDecl.getName().toString())) {
                    // check for parameters
                    params = methodDecl.getParameters();

                    if (params == null
                            || params.length() == 0) {
                        isEmptyConstructorPresent = true;
                        break;
                    }
                }
            }

            if (isEmptyConstructorPresent) {
                return;
            }

            final JCTree.JCMethodDecl emptyConstructor = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("<init>"),
                    null,
                    List.<JCTree.JCTypeParameter>nil(),
                    List.<JCTree.JCVariableDecl>nil(),
                    List.<JCTree.JCExpression>nil(),
                    mTreeMaker.Block(0L, List.<JCTree.JCStatement>nil()),
                    null
            );

            jcClassDecl.defs = jcClassDecl.defs.append(emptyConstructor);
        }

        private void addParcelableConstructor(JCTree.JCClassDecl jcClassDecl) {

            final JCTree.JCMethodDecl methodDecl = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("<init>"),
                    null,
                    List.<JCTree.JCTypeParameter>nil(),
                    List.of(mTreeMaker.VarDef(mASTHelper.getModifiers(), mASTHelper.getName(READ_PARCEL_VAR_NAME), mParcelType, null)),
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
                            mCallSuper ? List.of((JCTree.JCExpression) mTreeMaker.Ident(mASTHelper.getName(READ_PARCEL_VAR_NAME))) : List.<JCTree.JCExpression>nil()
                    )
            );

            return calls.append(callSuper).appendList(mStatements.readStatements);
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
                    mParcelableType,
                    mASTHelper.getName("Creator")
            );

            final JCTree.JCStatement returnInstanceStatement = mTreeMaker.Return(
                    mTreeMaker.NewClass(
                            null,
                            null,
                            mTreeMaker.Ident(jcClassDecl.name),
                            List.<JCTree.JCExpression>of(mTreeMaker.Ident(mASTHelper.getName(WRITE_PARCEL_VAR_NAME))),
                            null
                    )
            );

            JCTree.JCMethodDecl createFromParcel = mTreeMaker.MethodDef(
                    mASTHelper.getModifiers(Modifier.PUBLIC),
                    mASTHelper.getName("createFromParcel"),
                    mTreeMaker.Ident(jcClassDecl.name),
                    List.<JCTree.JCTypeParameter>nil(),
                    List.of(mTreeMaker.VarDef(mTreeMaker.Modifiers(0L), mASTHelper.getName(WRITE_PARCEL_VAR_NAME), mParcelType, null)),
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

            List<JCTree.JCStatement> statements = List.nil();

            // check if we should call super.writeToParcel()
            if (mCallSuper) {
                final JCTree.JCStatement superCall = mTreeMaker.Exec(
                        mTreeMaker.Apply(
                                List.<JCTree.JCExpression>nil(),
                                mTreeMaker.Select(mTreeMaker.Ident(mASTHelper.getName("super")), mASTHelper.getName("writeToParcel")),
                                List.of(
                                        (JCTree.JCExpression) mTreeMaker.Ident(mASTHelper.getName(WRITE_PARCEL_VAR_NAME)),
                                        mTreeMaker.Ident(mASTHelper.getName(WRITE_FLAGS_VAR_NAME))
                                )
                        )
                );
                statements = statements.append(superCall);
            }

            statements = statements.appendList(mStatements.writeStatements);

            final JCTree.JCVariableDecl varDest = mTreeMaker.VarDef(
                    mASTHelper.getModifiers(),
                    mASTHelper.getName(WRITE_PARCEL_VAR_NAME),
                    mParcelType,
                    null
            );

            final JCTree.JCVariableDecl varFlags = mTreeMaker.VarDef(
                    mASTHelper.getModifiers(),
                    mASTHelper.getName(WRITE_FLAGS_VAR_NAME),
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
                    mTreeMaker.Block(0L, statements),
                    null
            );

            jcClassDecl.defs = jcClassDecl.defs.append(writeToParcelMethod);
        }
    }

    static Statements buildStatements(ASTHelper astHelper, ParcelableData data) {

        final StatementCreatorFactory factory = new StatementCreatorFactory();
        final TreeMaker treeMaker = astHelper.getTreeMaker();

        final JCTree.JCExpression source    = treeMaker.Ident(astHelper.getName(READ_PARCEL_VAR_NAME));

        final JCTree.JCExpression dest      = treeMaker.Ident(astHelper.getName(WRITE_PARCEL_VAR_NAME));
        final JCTree.JCExpression flags     = treeMaker.Ident(astHelper.getName(WRITE_FLAGS_VAR_NAME));

        List<JCTree.JCStatement> read = List.nil();
        List<JCTree.JCStatement> write = List.nil();

        boolean isArray;
        ParcelableType type;

        for (ParcelableItem item: data.items) {

            isArray = false;
            type = item.type;

            if (type == ParcelableType.ARRAY) {
                isArray = true;
                type = ((ParcelableItemArray)item).arrayType;
            }

            final StatementCreator creator = factory.get(type);
            read = read.appendList(creator.createReadFromParcel(astHelper, data.element, source, item.name, isArray));
            write = write.appendList(creator.createWriteToParcel(astHelper, data.element, dest, flags, item.name, isArray));
        }

        return new Statements(read, write);
    }

    private static class Statements {

        final List<JCTree.JCStatement> readStatements;
        final List<JCTree.JCStatement> writeStatements;

        Statements(List<JCTree.JCStatement> readStatements, List<JCTree.JCStatement> writeStatements) {
            this.readStatements = readStatements;
            this.writeStatements = writeStatements;
        }
    }
}
