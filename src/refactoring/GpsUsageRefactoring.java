package Refactoring;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public class GpsUsageRefactoring extends Refactoring {

    public GpsUsageRefactoring(){
        super();
        this.name = "Usage of GPS";
        this.dependencyFiles = new String[]{"BatteryAwarenessCriteria.java", "LocationManagerBatteryAwareness.java", "BatteryUtils.java"};
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
                            refactoringCandidates.add(new RefactoringCandidate(expression));
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
    public void refactor(RefactoringCandidate candidate) {
        PsiElement element = candidate.getElement();
        DependencyCreator dependencyCreator = new DependencyCreator();
        dependencyCreator.createPackageAndFiles(element,dependencyFiles);
        ArrayList<PsiClass> dependencyClasses = dependencyCreator.createPackageAndFiles(element,dependencyFiles);
        PsiClass classOfTimer = JavaPsiFacade.getInstance(element.getProject()).findClass("energyRefactorings.LocationManagerBatteryAwareness", GlobalSearchScope.allScope(element.getProject()));
        if (classOfTimer == null) {
            classOfTimer = getClassFromList(dependencyClasses, "LocationManagerBatteryAwareness");
        }
        for (PsiElement child : element.getChildren())
            if (child instanceof PsiExpressionList){
                PsiExpressionList argumentsExpressionList = (PsiExpressionList)child;
                PsiElement[] argumentsCompleteArray = argumentsExpressionList.getChildren();
                PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(element.getProject());
                PsiElement identifier = getVariableOfMethodCall(element);

                StringBuilder statementText = new StringBuilder("LocationManagerBatteryAwareness batteryAwareness = new LocationManagerBatteryAwareness(");
                statementText.append(identifier.getText());
                statementText.append(",");
                statementText.append(createCustomizationString(candidate.getBatteryAwarenessCriteria()));
                statementText.append(",new Object[]");
                statementText.append(Utilities.elementsArrayToString(argumentsCompleteArray));
                statementText.append(");");
                PsiElement newElement = elementFactory.createStatementFromText(statementText.toString(),argumentsExpressionList);
                WriteCommandAction.runWriteCommandAction(element.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        element.getParent().getParent().addAfter(newElement, element.getParent());
                    }
                });
                dependencyCreator.insertImportStatement(element, "LocationManagerBatteryAwareness", "energyRefactorings");
                dependencyCreator.insertImportStatement(element, "BatteryAwarenessCriteria", "energyRefactorings");
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

    public String getEffectText(RefactoringCandidate candidate){
        BatteryAwarenessCriteria criteria = candidate.getBatteryAwarenessCriteria();
        StringBuilder result = new StringBuilder("A LocationManagerBatteryAwareness-object will be added to your code.<br>" +
                "It will check every 10 minutes if the settings of the way the app gets location info are still in line with the following power customizations:<br>" +
                candidate.getBatteryAwarenessCriteria().toString() +
                "<br><br>This means that as long as the battery is above");
        switch (criteria.getPowerSafeScheme()) {
            case POWER_SAFE_LOW:
                result.append(" 25% ");
                break;
            case POWER_SAFE_MEDIUM:
                result.append(" 50% ");
                break;
            case POWER_SAFE_HIGH:
                result.append(" 75% ");
                break;
        }
        result.append("the updates will be executed as programmed.<br>");
        result.append("When the battery goes <b>below");
        switch (criteria.getPowerSafeScheme()) {
            case POWER_SAFE_LOW:
                result.append(" 25%");
                break;
            case POWER_SAFE_MEDIUM:
                result.append(" 50%");
                break;
            case POWER_SAFE_HIGH:
                result.append(" 75%");
                break;
        }
        result.append(", updates will be slowed down linearly until 10x slower</b>.<br>");
        result.append("If the device goes into power-saving mode, <b>the location updates will ");
        if (!criteria.getSuspendIfInBatterySafeMode()){
            result.append("not ");
        }
        result.append("be reduced to once per hour</b>.<br>");
        result.append("If the battery level goes <b>below ");
        result.append(Integer.toString(criteria.getSuspendThreshold()));
        result.append("%, the location updates will be reduced to once per hour</b>.<br>");
        return result.toString();
    }

    private @Nullable
    PsiClass getClassFromList(ArrayList<PsiClass> dependencyClasses, String batteryAwareTimerTask) {
        for (PsiClass psiClass : dependencyClasses){
            if (psiClass.getName().equalsIgnoreCase("BatteryAwareTimerTask")){
                return psiClass;
            }
        }
        return null;
    }

    private String createCustomizationString(BatteryAwarenessCriteria criteria){
        StringBuilder result = new StringBuilder("new BatteryAwarenessCriteria(BatteryAwarenessCriteria.PowerSaveScheme.POWER_SAFE_");
        switch (criteria.getPowerSafeScheme()){
            case POWER_SAFE_LOW:
                result.append("LOW");
                break;
            case POWER_SAFE_MEDIUM:
                result.append("MEDIUM");
                break;
            case POWER_SAFE_HIGH:
                result.append("HIGH");
                break;
        }
        if (criteria.getSuspendIfInBatterySafeMode()) {
            result.append(",true");
        } else {
            result.append(",false");
        }
        result.append("," + Integer.toString(criteria.getSuspendThreshold()) + ")");
        return result.toString();
    }

    private @Nullable PsiElement getVariableOfMethodCall(PsiElement methodcall){
        PsiElement reference = methodcall.getChildren()[0];
        for (PsiElement child : reference.getChildren()){
            if (child instanceof PsiReferenceExpression){
                for (PsiElement subChild : child.getChildren()){
                    if (subChild instanceof PsiIdentifier){
                        return subChild;
                    }
                }
            }
        }
        return null;
    }
}
