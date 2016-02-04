import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import java.util.HashSet;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public abstract class Refactoring {

    protected HashSet<PsiElement> foundElements;
    protected HashSet<PsiElement> elementsToRefactor;

    protected String name;

    protected int noOfElementsScanned;

    public HashSet<PsiElement> getElementsToRefactor() {
        return elementsToRefactor;
    }

    public HashSet<PsiElement> getFoundElements() {
        return foundElements;
    }

    public int getNoOfElementsScanned() {
        return noOfElementsScanned;
    }

    public Refactoring(){
        foundElements = new HashSet<PsiElement>();
        elementsToRefactor = new HashSet<PsiElement>();
        noOfElementsScanned = 0;
    }

    public abstract JavaRecursiveElementVisitor getDetector();
    public abstract void refactor(PsiElement element);
    public abstract boolean isAlreadyRefactored(PsiElement element);

}
