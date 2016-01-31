import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import java.util.HashSet;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public abstract class Refactoring {

    public HashSet<PsiElement> foundElements;
    public HashSet<PsiElement> elementsToRefactor;

    public HashSet<PsiElement> getElementsToRefactor() {
        return elementsToRefactor;
    }

    public HashSet<PsiElement> getFoundElements() {
        return foundElements;
    }

    public Refactoring(){
        foundElements = new HashSet<PsiElement>();
        elementsToRefactor = new HashSet<PsiElement>();
    }

    public abstract JavaRecursiveElementVisitor getDetector();
    public abstract void refactor(PsiElement element);

}
