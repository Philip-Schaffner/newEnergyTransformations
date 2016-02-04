import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public class GpsUsageRefactoring extends Refactoring {

    public GpsUsageRefactoring(){
        super();
    }

    public JavaRecursiveElementVisitor getDetector(){
        return new JavaRecursiveElementVisitor() {
//            public void visitLocalVariable(PsiLocalVariable variable) {
//                final PsiType psiType = variable.getType();
//                if (psiType instanceof PsiClassType) {
//                    final PsiClassType psiClassType = (PsiClassType) psiType;
//                    final PsiClass psiClassOfField = psiClassType.resolve();
//                    if (psiClassOfField.getQualifiedName().equalsIgnoreCase("android.location.LocationManager")){
//                        System.out.println("found gps usage");
//                    }
//
//                }
//            }

            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                noOfElementsScanned++;
                try {
                    PsiClass classOfExpression = expression.resolveMethod().getContainingClass();
                    if (!(classOfExpression instanceof PsiAnonymousClass)){
                        String qualifiedClassName = classOfExpression.getQualifiedName();
                        String nameOfMethod = expression.resolveMethod().getName();
                        if ((qualifiedClassName.equalsIgnoreCase("android.location.LocationManager")
                                && nameOfMethod.equalsIgnoreCase("requestLocationUpdates"))) {
                            foundElements.add(expression);
                        }
                    }
                } catch (Exception e){
                    System.out.println(expression.getText() + " doesnt have class/method name assigned");
                }
            }
        };


    }

    @Override
    public boolean isAlreadyRefactored(PsiElement element){
        if (Utilities.findMethodReferenceInChildren(element, "energyRefactorings.ContextAwareLocationManager", "getContextAwareProvider") != null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void refactor(PsiElement element) {
        DependencyCreator dependencyCreator = new DependencyCreator();
        String[] filesToCreate = new String[]{"ContextAwareLocationManager.java"};
        dependencyCreator.createPackageAndFiles(element,filesToCreate);
        for (PsiElement child : element.getChildren())
            if (child instanceof PsiExpressionList){
                PsiExpressionList argumentsExpressionList = (PsiExpressionList)child;
                PsiElement[] argumentsCompleteArray = argumentsExpressionList.getChildren();
                PsiElement[] argumentsArray = removeJavaTokens(argumentsCompleteArray);
                int overloadVariant = checkOverloadVariant(argumentsArray);
                PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(element.getProject());
                switch(overloadVariant){
                    case 1:
                        PsiElement newCriteria = elementFactory.createExpressionFromText("ContextAwareLocationManager.getContextAwareCriteria()",null);
                        replaceArgumentNo(argumentsCompleteArray,3,newCriteria);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        PsiElement newProvider = elementFactory.createExpressionFromText("ContextAwareLocationManager.getContextAwareProvider()",null);
                        replaceArgumentNo(argumentsCompleteArray,1,newProvider);
                        break;
                }
                dependencyCreator.insertImportStatement(element, "ContextAwareLocationManager", "energyRefactorings");
            }
    }

    private PsiElement[] removeJavaTokens(PsiElement[] argumentsCompleteArray) {
        ArrayList<PsiElement> result = new ArrayList<>();
        for (PsiElement argument : argumentsCompleteArray){
            if (!(argument instanceof PsiJavaToken || argument instanceof PsiWhiteSpace)){
                result.add(argument);
            }
        }
        return result.toArray(new PsiElement[result.size()]);
    }

    private int checkOverloadVariant(PsiElement[] argumentArray) {
//1.(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
//2.(String provider, long minTime, float minDistance, LocationListener listener)
//3.(String provider, long minTime, float minDistance, LocationListener listener, Looper looper)
//4.(String provider, long minTime, float minDistance, PendingIntent intent)
        PsiType[] types = new PsiType[argumentArray.length];
        int index = 0;
        for (PsiElement argument : argumentArray){
            if (argument instanceof PsiReferenceExpression) {
                PsiType type = ((PsiField) ((PsiReferenceExpressionImpl) argument).resolve()).getType();
                types[index] = type;
            } else if (argument instanceof PsiMethodCallExpression){
                PsiType type = ((PsiMethodCallExpressionImpl) argument).resolveMethod().getReturnType();
                types[index] = type;
            } else if (argument instanceof PsiLiteralExpression){
                PsiType type = ((PsiLiteralExpression) argument).getType();
                types[index] = type;
            } else if (argument instanceof PsiThisExpression){
                PsiType type = ((PsiThisExpression) argument).getType();
                types[index] = type;
            }
            index++;
        }

        if (types[0].getPresentableText().equalsIgnoreCase("String")){
            if (argumentArray.length == 5){
                return 3;
            } else if (Utilities.checkIfHasSupertype(types[3],"LocationListener")){
                return 4;
            } else {
                return 2;
            }
        } else {
            return 1;
        }
    }

    private void replaceArgumentNo(PsiElement[] list, int number, PsiElement newElement){
        int argumentNumber = 1;
        for (PsiElement argument : list){
            if (argument instanceof PsiJavaToken && ((PsiJavaToken) argument).getTokenType().toString().equalsIgnoreCase("COMMA")){
                argumentNumber++;
            } else if (argument instanceof PsiWhiteSpace || argument instanceof PsiComment
                    || argument instanceof PsiJavaToken){
                continue;
            } else if (argumentNumber == number){
                WriteCommandAction.runWriteCommandAction(argument.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        argument.replace(newElement);
                    }
                });
            }
        }
    }
}
