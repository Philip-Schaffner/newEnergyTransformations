import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
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
        psiFile.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
            }


            /*@Override
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                IElementType opSign = expression.getOperationTokenType();
                if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
                    PsiExpression lOperand = expression.getLOperand();
                    PsiExpression rOperand = expression.getROperand();
                    if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) return;

                    PsiType lType = lOperand.getType();
                    PsiType rType = rOperand.getType();

                    if (isCheckedType(lType) || isCheckedType(rType)) {
                        holder.registerProblem(expression,
                                DESCRIPTION_TEMPLATE, myQuickFix);
                    }
                }
            }*/

            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                if (annotation.getNameReferenceElement().getText().equalsIgnoreCase("POST")) {
                    PsiElement annotatedElement = annotation.getParent().getParent();
                    if (annotatedElement instanceof PsiMethod) {
                        visitSuspiciousElement(annotatedElement);
                    }
                }
            }

            @Nullable
            public void inspectMethodCallExpression(PsiMethodCallExpression expression) {
                PsiExpression[] exList = expression.getArgumentList().getExpressions();
                for (PsiExpression ex : exList) {
                    for (PsiElement param : ex.getChildren()) {
//                        if (getVariable(param) != null) {
//                            PsiVariable var = getVariable(param);
//
                    }
                    if (ex.getType() instanceof PsiClassReferenceType) {
                        PsiClass aClass = ((PsiClassReferenceType) ex.getType()).resolve();
                        aClass.getContainingFile().getVirtualFile();
                        if ((aClass.getQualifiedName().equals("Dummy"))) {
                            //System.out.println("found a " + aClass.getName());
                        } else if (aClass.getQualifiedName().equals("android.location.LocationManager")) {
                            //System.out.println("found a " + aClass.getName());
                        }

                    }
                }
            }

            private PsiExpression getExpression(PsiCodeBlock body) {
                final PsiStatement[] statements = body.getStatements();
                if (statements.length == 1) {
                    if (statements[0] instanceof PsiBlockStatement) {
                        return getExpression(((PsiBlockStatement) statements[0]).getCodeBlock());
                    }
                    if (statements[0] instanceof PsiReturnStatement || statements[0] instanceof PsiExpressionStatement) {
                        if (statements[0] instanceof PsiReturnStatement) {
                            final PsiReturnStatement returnStatement = (PsiReturnStatement) statements[0];
                            return returnStatement.getReturnValue();
                        } else {
                            final PsiExpression expression = ((PsiExpressionStatement) statements[0]).getExpression();
                            final PsiType psiType = expression.getType();
                            if (psiType != PsiType.VOID) {
                                return null;
                            }
                            return expression;
                        }
                    }
                }
                return null;
            }

            private PsiVariable getVariable(PsiElement element) {
                if (!(element instanceof PsiJavaToken)) {
                    return null;
                }

                final PsiJavaToken token = (PsiJavaToken) element;
                final PsiElement parent = token.getParent();
                if (parent instanceof PsiVariable) {
                    return (PsiVariable) parent;
                }


                if (parent instanceof PsiReferenceExpression) {
                    final PsiReferenceExpression referenceExpression = (PsiReferenceExpression) parent;
                    final PsiElement resolvedReference = referenceExpression.resolve();
                    if (resolvedReference instanceof PsiVariable) {
                        return (PsiVariable) resolvedReference;
                    }
                }


                if (parent instanceof PsiJavaCodeReferenceElement) {
                    final PsiJavaCodeReferenceElement javaCodeReferenceElement = (PsiJavaCodeReferenceElement) parent;
                    return (PsiVariable) PsiTreeUtil.getParentOfType(javaCodeReferenceElement, PsiVariable.class);
                }
                return null;
            }
        });
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
                        System.out.println("found GET in while loop");
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
}
