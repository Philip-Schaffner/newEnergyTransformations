import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

/**
 * Created by pip on 17.12.2015.
 */
public class FindGPSUsage extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        psiFile.accept(new JavaRecursiveElementVisitor() {
            public void visitLocalVariable(PsiLocalVariable variable) {
//                DataContext dataContext = DataManager.getInstance().getDataContext();
//                Project project = DataKeys.PROJECT.getData(dataContext);
//                PsiManager psiManager = PsiManager.getInstance(project);
//                String classQName = "android.location.LocationManager";
//                PsiClass locationClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(classQName, GlobalSearchScope.allScope(project));
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
//                if (expression.getType() instanceof PsiClassReferenceType) {
//                    if (expression.getText().equalsIgnoreCase("lm")) {
//                        PsiClass aClass = ((PsiClassReferenceType) expression.getType()).resolve();
//                        if (aClass != null && aClass.getName() != null && aClass.getName().equalsIgnoreCase("android.location.LocationManager")) {
//                            Collection<PsiReference> locationUsages = findUsages(aClass);
//                        }
//                    }
//                }
//                PsiManager psiManager = PsiManager.getInstance();
//                PsiExpression[] exList = expression.getArgumentList().getExpressions();
//                for (PsiExpression ex : exList) {
//                    if (ex.getType() instanceof PsiClassReferenceType) {
//                        PsiClass aClass = ((PsiClassReferenceType) ex.getType()).resolve();
//                        aClass.getContainingFile().getVirtualFile();
//                        if (aClass.getQualifiedName().equals("android.location.LocationManager")) {
//                            System.out.println("found a " + aClass.getName());
//                        }
//                    }
//                }
            }
        });
    }
}
