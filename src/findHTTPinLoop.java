import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by pip on 17.12.2015.
 */
public class findHTTPinLoop extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile != null) {
            JavaRecursiveElementVisitor javaRecursiveElementVisitor = new JavaRecursiveElementVisitor() {
                @Override
                public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
                }

                @Override
                public void visitAnnotation(PsiAnnotation annotation) {
                    if (annotation.getNameReferenceElement().getText().equalsIgnoreCase("POST")) {
                        PsiElement annotatedElement = annotation.getParent().getParent();
                        if (annotatedElement instanceof PsiMethod) {
                            visitSuspiciousElement(annotatedElement);
                        }
                    }
                }
            };
            psiFile.accept(javaRecursiveElementVisitor);
        }
    }

    private void visitSuspiciousElement(PsiElement annotatedElement) {
        Collection<PsiReference> usages = findUsages(annotatedElement);
        for (PsiReference ref : usages){
            if (ref instanceof PsiReferenceExpression) {
                PsiElement[] children = ((PsiReferenceExpression) ref).getChildren();
                for (PsiElement child : children) {
                    while (!(child instanceof PsiWhileStatement) && child.getParent() != null) {
                        if (child instanceof PsiMethod && ((PsiMethod) child).getName().equalsIgnoreCase("update")){
                            visitSuspiciousElement(child);
                        }
                        child = child.getParent();
                    }
                    if (child instanceof PsiWhileStatement) {
                        highlightElement(annotatedElement);
                        
                    }
                }
            }
        }
    }

    private Collection<PsiReference> findUsages(PsiElement element) {
        JavaFindUsagesProvider usagesProvider = new JavaFindUsagesProvider();
        usagesProvider.canFindUsagesFor(element);
        Query<PsiReference> query = ReferencesSearch.search(element);
        Collection<PsiReference> result = query.findAll();
        return result;
    }

    private static void highlightElement(@NotNull PsiElement element)
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
        final PsiElement[] elements = new PsiElement[]{element};
        highlightManager.addOccurrenceHighlights(
                editor, elements, textattributes, true, null);
        final WindowManager windowManager = WindowManager.getInstance();
        final StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("Press Esc to remove highlighting");
    }
}
