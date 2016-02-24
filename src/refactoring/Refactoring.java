package Refactoring;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;

import java.util.HashSet;

/**
 * Created by pip on 26.01.2016.
 */
public abstract class Refactoring {

    protected HashSet<PsiElement> foundElements;
    protected HashSet<RefactoringCandidate> refactoringCandidates;

    protected String name;
    protected String[] dependencyFiles;

    protected int noOfElementsScanned;

    public HashSet<RefactoringCandidate> getRefactoringCandidates() {
        return refactoringCandidates;
    }

    public HashSet<PsiElement> getFoundElements() {
        return foundElements;
    }

    public int getNoOfElementsScanned() {
        return noOfElementsScanned;
    }

    public String getName() {
        return name;
    }

    public Refactoring(){
        foundElements = new HashSet<PsiElement>();
        refactoringCandidates = new HashSet<RefactoringCandidate>();
        noOfElementsScanned = 0;
    }

    public abstract JavaRecursiveElementVisitor getDetector();
    public abstract void refactor(RefactoringCandidate candidate);
    public abstract boolean isAlreadyRefactored(PsiElement element);
    public abstract String getEffectText(RefactoringCandidate candidate);

}
