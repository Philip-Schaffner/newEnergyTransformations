import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;

import java.util.ArrayList;
import java.util.Collection;

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
                        if (visitSuspiciousElement(annotatedElement)){
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

    }

    private boolean visitSuspiciousElement(PsiElement annotatedElement) {
        if (annotatedElement instanceof PsiClass && ((PsiClass) annotatedElement).getQualifiedName().equalsIgnoreCase("java.util.TimerTask")){
            return true;
        }
        Collection<PsiReference> usages = findUsages(annotatedElement);
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

    private boolean checkIfHasLoopParent(PsiElement element){
        while (element.getParent() != null) {
            if (element instanceof PsiWhileStatement) {
                elementsToRefactor.add(element);
                return true;
            } else if (element instanceof PsiMethod && visitSuspiciousElement(element)) {
                elementsToRefactor.add(element);
                return true;
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

    private Collection<PsiReference> findUsages(PsiElement element) {
        JavaFindUsagesProvider usagesProvider = new JavaFindUsagesProvider();
        usagesProvider.canFindUsagesFor(element);
        Query<PsiReference> query = ReferencesSearch.search(element);
        Collection<PsiReference> result = query.findAll();
        return result;
    }
}
