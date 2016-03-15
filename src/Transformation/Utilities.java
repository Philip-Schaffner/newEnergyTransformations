package Transformation;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by pip on 26.01.2016.
 */
public class Utilities {
    private static void highlightElement(Document document, @NotNull PsiElement element)
    {
        final Project project = element.getProject();
        final FileEditorManager editorManager =
                FileEditorManager.getInstance(project);
        final HighlightManager highlightManager =
                HighlightManager.getInstance(project);
        final EditorColorsManager editorColorsManager =
                EditorColorsManager.getInstance();
        final Editor editor = editorManager.getSelectedTextEditor();
        final EditorColorsScheme globalScheme =
                editorColorsManager.getGlobalScheme();
        final TextAttributes textattributes =
                globalScheme.getAttributes(
                        EditorColors.SEARCH_RESULT_ATTRIBUTES);
//        final PsiElement[] elements = new PsiElement[]{element};
//        highlightManager.addOccurrenceHighlights(
//                editor, elements, textattributes, true, null);
//        final WindowManager windowManager = WindowManager.getInstance();
//        final StatusBar statusBar = windowManager.getStatusBar(project);
//        statusBar.setInfo("Press Esc to remove highlighting");
        int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
        final TextAttributes textattributes_2 = globalScheme.getAttributes(
                EditorColors.SEARCH_RESULT_ATTRIBUTES);
        editor.getMarkupModel().addLineHighlighter(lineNum, HighlighterLayer.CARET_ROW, textattributes);

        System.out.println(("found something @ " + lineNum));
    }

    public static Collection<PsiReference> findUsages(PsiElement element) {
        JavaFindUsagesProvider usagesProvider = new JavaFindUsagesProvider();
        usagesProvider.canFindUsagesFor(element);
        Query<PsiReference> query = ReferencesSearch.search(element);
        Collection<PsiReference> result = query.findAll();
        return result;
    }

    public static @Nullable PsiElement findInParents(PsiElement element, Class type){
        while (!(element.getClass() == type)
            && (element.getParent() != null)){
                element = element.getParent();
            }
        return element;
    }

    public static @Nullable PsiElement findInChildren(PsiElement element, Class elementClass){
        LinkedList<PsiElement> queue = new LinkedList<PsiElement>();
        queue.add(element);
        while (!queue.isEmpty()){
            PsiElement child = queue.remove();
            if (child.getClass() == elementClass){
                return child;
            } else {
                for (PsiElement n : child.getChildren()){
                    queue.add(n);
                }
            }
        }
        return null;
    }

    public static boolean checkIfImportExists(PsiImportStatementBase[] importList, String s) {
        for (PsiImportStatementBase statement : importList){
            if (statement.getText().contains(s)){
                return true;
            }
        }
        return false;
    }

    public static ArrayList<PsiElement> getArguments(PsiExpressionList expressionList){
        return new ArrayList<>();
    }

    public static boolean checkIfHasSupertype(PsiType typetoCheck, String typeToFind){
        if (typetoCheck.getPresentableText().equalsIgnoreCase(typeToFind)){
            return true;
        } else {
            PsiType[] supertypes = typetoCheck.getSuperTypes();
            for (PsiType type : supertypes) {
                if (type.getPresentableText().equalsIgnoreCase(typeToFind)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @Nullable static PsiMethodCallExpression findMethodReferenceInChildren(PsiElement element, String className, String methodName) {
        LinkedList<PsiElement> queue = new LinkedList<PsiElement>();
        queue.add(element);
        while (!queue.isEmpty()){
            PsiElement child = queue.remove();
            if (child instanceof PsiMethodCallExpression) {
                PsiClass classOfExpression = ((PsiMethodCallExpression)child).resolveMethod().getContainingClass();
                String nameOfMethod = ((PsiMethodCallExpression)child).resolveMethod().getName();
                if ((classOfExpression.getQualifiedName().equalsIgnoreCase(className)
                        && nameOfMethod.equalsIgnoreCase(methodName))){
                    return (PsiMethodCallExpression)child;
                }
            }
            for (PsiElement n : child.getChildren()) {
                queue.add(n);
            }
        }
        return null;
    }

    public @Nullable static PsiExpressionList getArgumentsOfMethod(PsiMethodCallExpression methodCallExpression){
        PsiElement[] items = methodCallExpression.getChildren();
        for (PsiElement item : items){
            if (item instanceof PsiExpressionList){
                return (PsiExpressionList)item;
            }
        }
        return null;
    }




    public @Nullable static PsiElement getArgumentNo(PsiExpressionList list, int number){
        int argumentNumber = 1;
        for (PsiElement argument : list.getChildren()){
            if (argument instanceof PsiJavaToken && ((PsiJavaToken) argument).getTokenType().toString().equalsIgnoreCase("COMMA")){
                argumentNumber++;
            } else if (argument instanceof PsiWhiteSpace || argument instanceof PsiComment
                    || argument instanceof PsiJavaToken){
                continue;
            } else if (argumentNumber == number){
                return argument;
            }
        }
        return null;
    }

    public static String elementsArrayToString(PsiElement[] list){
        StringBuilder result = new StringBuilder("{");
        for (int i = 1; i < list.length -1; i++){
            result.append(list[i].getText());
        }
        result.append("}");
        return result.toString();
    }
}
