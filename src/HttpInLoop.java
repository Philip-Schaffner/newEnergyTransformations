import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.richcopy.model.OutputInfoSerializer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiImportStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * Created by pip on 26.01.2016.
 */
public class HttpInLoop extends Refactoring {

    public HttpInLoop(){
        super();
    }

    @Override
    public JavaRecursiveElementVisitor getDetector() {
        return new JavaRecursiveElementVisitor() {

            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if (annotation.getNameReferenceElement().getText().equalsIgnoreCase("POST")) {
                    PsiElement annotatedElement = annotation.getParent().getParent();
                    if (annotatedElement instanceof PsiMethod) {
                        if (checkIfHasLoopParent(annotatedElement)){
                            foundElements.add(annotatedElement);
                        }
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                try {
                    PsiClass classOfExpression = expression.resolveMethod().getContainingClass();
                    String nameOfMethod = expression.resolveMethod().getName();
                    if ((classOfExpression.getQualifiedName().equalsIgnoreCase("java.net.URL")
                            && nameOfMethod.equalsIgnoreCase("openConnection"))
                            || (classOfExpression.getQualifiedName().equalsIgnoreCase("java.net.URLConnection")
                            && nameOfMethod.equalsIgnoreCase("connect"))
                            || (classOfExpression.getQualifiedName().equalsIgnoreCase("org.apache.http.client.HttpClient")
                            && nameOfMethod.equalsIgnoreCase("execute"))
                            || (classOfExpression.getQualifiedName().equalsIgnoreCase("java.net.Socket")
                            && nameOfMethod.equalsIgnoreCase("getOutputStream"))
                            || (classOfExpression.getQualifiedName().equalsIgnoreCase("com.google.android.gms.ads.AdView")
                            && nameOfMethod.equalsIgnoreCase("loadAd"))) {
                        if (checkIfHasLoopParent(expression)) {
                            foundElements.add(expression);
                        }
                    }

                } catch (Exception e){
                    System.out.println(e);
                }
            }
        };

    }



    @Override
    public void refactor(PsiElement element) {
        DependencyCreator dependencyCreator = new DependencyCreator();
        String[] filesToCreate = new String[]{"SleepTimeCalculator.java"};
        dependencyCreator.createPackageAndFiles(element,filesToCreate);
        if (element instanceof PsiLocalVariable) {
            PsiType psiType = ((PsiLocalVariable) element).getType();
            if (psiType instanceof PsiClassType) {
                String className = ((PsiClassType) psiType).resolve().getQualifiedName();
                Collection<PsiReference> usages = Utilities.findUsages(element);
                switch (className) {
                    case "java.util.TimerTask":
                        for (PsiReference usage : usages) {
                            PsiElement usageElement = usage.getElement();
                            while (!(usageElement == null
                                    || (usageElement instanceof PsiMethodCallExpression))) {
                                usageElement = usageElement.getParent();
                            }
                            replaceTimer(usageElement);
                            dependencyCreator.insertImportStatement(usageElement, "SleepTimeCalculator", "energyRefactorings");
                        }
                        break;
                    case "android.os.AsyncTask":
                        for (PsiReference usage : usages) {
                            PsiElement usageElement = usage.getElement();
                            while (!(usageElement == null
                                    || (usageElement instanceof PsiMethodCallExpression))) {
                                usageElement = usageElement.getParent();
                            }
                            if (usageElement instanceof PsiMethodCallExpression
                                    && usageElement.getText().contains("schedule")) {
                                PsiLiteralExpression timer = (PsiLiteralExpression) Utilities.findInChildren(usageElement, PsiLiteralExpressionImpl.class);
                                PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(usageElement.getProject());
                                PsiElement newTimer = elementFactory.createExpressionFromText("SleepTimeCalculator.getSleepTimer()", usageElement);
                                PsiElement importStatement = elementFactory.createImportStatement(elementFactory.createClass("SleepTimeCalculator"));
                                PsiJavaFile timerFile = (PsiJavaFile) timer.getContainingFile();
                                PsiImportStatementBase[] importList = timerFile.getImportList().getAllImportStatements();
                                PsiImportStatementBase lastImport = importList[importList.length - 1];
                                WriteCommandAction.runWriteCommandAction(usageElement.getProject(), new Runnable() {
                                    @Override
                                    public void run() {
                                        timer.replace(newTimer);
                                        timerFile.addAfter(lastImport, importStatement);
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        }
    }

    private void replaceTimer(PsiElement usageElement) {
        if (usageElement instanceof PsiMethodCallExpression
                && usageElement.getText().contains("schedule")) {
            PsiExpression[] arguments = ((PsiMethodCallExpression) usageElement).getArgumentList().getExpressions();
            PsiExpression timer = arguments[1];
            PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(usageElement.getProject());
            PsiJavaFile timerFile = (PsiJavaFile) timer.getContainingFile();
            if (!timer.getText().contains("getSleepTimer")) {
                PsiElement newTimer = elementFactory.createExpressionFromText(timer.getText() + "*SleepTimeCalculator.getSleepTimer()", usageElement);
                PsiComment comment = elementFactory.createCommentFromText("//refactored by EnergyRefactorings ", null);
                WriteCommandAction.runWriteCommandAction(usageElement.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        PsiElement anchor = timer.replace(newTimer);
                        timerFile.addBefore(comment, anchor);
                    }
                });
            }
        }
    }


    private boolean checkIfHasLoopParent(PsiElement element){
        while (element.getParent() != null) {
            if (element instanceof PsiWhileStatement) {
                elementsToRefactor.add(element);
                return true;
            } else if (element instanceof PsiMethod){
                if (visitAllUsages(element)) {
//                elementsToRefactor.add(element);
                    return true;
                }
            } else if (element instanceof PsiLocalVariable){
                PsiType psiType = ((PsiLocalVariable) element).getType();
                if (psiType instanceof PsiClassType){
                    String className = ((PsiClassType)psiType).resolve().getQualifiedName();
                    PsiType[] implementsListTypes = ((PsiClassType)psiType).resolve().getImplementsListTypes();
                    ArrayList<String> implementsNames = new ArrayList<String>();
                    for (PsiType type : implementsListTypes){
                        if (type instanceof PsiClassType){
                            implementsNames.add(((PsiClassType) type).resolve().getQualifiedName());
                        }
                    }
                    if (className.equalsIgnoreCase("java.util.TimerTask")
                            || className.equalsIgnoreCase("android.os.AsyncTask")
                            || className.equalsIgnoreCase("java.lang.Runnable")
                            || implementsNames.contains("java.lang.Runnable")){
                        elementsToRefactor.add(element);
                        return true;
                    }
                }
            }
            element = element.getParent();
        }
        return false;
    }

    private boolean visitAllUsages(PsiElement element) {
        Collection<PsiReference> usages = Utilities.findUsages(element);
        for (PsiReference ref : usages){
            if (ref instanceof PsiReferenceExpression) {
                PsiElement[] children = ((PsiReferenceExpression) ref).getChildren();
                for (PsiElement child : children) {
                    return checkIfHasLoopParent(child);
                }
            }
        }
        return false;
    }
}
