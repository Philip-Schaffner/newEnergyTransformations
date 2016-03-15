package Transformation;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;

import java.util.HashSet;

/**
 * Created by pip on 26.01.2016.
 */
public abstract class Transformation {

    protected HashSet<PsiElement> foundElements;
    protected HashSet<TransformationCandidate> transformationCandidates;

    protected String name;
    protected String[] dependencyFiles;

    protected int noOfElementsScanned;

    public HashSet<TransformationCandidate> getTransformationCandidates() {
        return transformationCandidates;
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

    public Transformation(){
        foundElements = new HashSet<PsiElement>();
        transformationCandidates = new HashSet<TransformationCandidate>();
        noOfElementsScanned = 0;
    }

    public abstract JavaRecursiveElementVisitor getDetector();
    public abstract void refactor(TransformationCandidate candidate);
    public abstract boolean isAlreadyRefactored(PsiElement element);
    public abstract String getEffectText(TransformationCandidate candidate);

}
