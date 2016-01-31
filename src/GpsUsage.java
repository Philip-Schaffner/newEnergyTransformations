import com.intellij.psi.*;

/**
 * Created by pip on 26.01.2016.
 */
public class GpsUsage extends Refactoring {

    public GpsUsage(){
        super();
    }

    public JavaRecursiveElementVisitor getDetector(){
        return new JavaRecursiveElementVisitor() {
            public void visitLocalVariable(PsiLocalVariable variable) {
                if(variable.getNameIdentifier().getText().equalsIgnoreCase("lm")) {
                    final PsiType psiType = variable.getType();
                    if (psiType instanceof PsiClassType) {
                        final PsiClassType psiClassType = (PsiClassType) psiType;
                        final PsiClass psiClassOfField = psiClassType.resolve();
                        if (psiClassOfField.getQualifiedName().equalsIgnoreCase("android.location.LocationManager")){
                            System.out.println("found gps usage");
                        }

                    }
                }
            }
        };
    }

    @Override
    public void refactor(PsiElement element) {
        System.out.println("refactoring gps");
    }
}
