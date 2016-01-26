import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public abstract class Refactoring {

    public ArrayList<PsiElement> foundElements;
    public ArrayList<PsiElement> elementsToRefactor;

    public Refactoring(){
        foundElements = new ArrayList<PsiElement>();
        elementsToRefactor = new ArrayList<PsiElement>();
    }

    public JavaRecursiveElementVisitor getDetector(){
        return new JavaRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
            }
        };
    }
    public void refactor(PsiElement element){
        System.out.println(element);
    };

}
