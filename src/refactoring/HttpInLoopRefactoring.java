package Refactoring;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by pip on 26.01.2016.
 */
public class HttpInLoopRefactoring extends Refactoring {

    public HttpInLoopRefactoring(){
        super();
        this.name = "HTTP calls inside a loop";
        this.dependencyFiles = new String[]{"BatteryAwarenessCriteria.java", "BatteryAwareTimerTask.java", "BatteryUtils.java"};
    }

    @Override
    public JavaRecursiveElementVisitor getDetector() {
        return new JavaRecursiveElementVisitor() {

            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                noOfElementsScanned++;
                if (annotation.getNameReferenceElement().getText().equalsIgnoreCase("POST")) {
                    PsiElement annotatedElement = annotation.getParent().getParent();
                    if (annotatedElement instanceof PsiMethod) {
                        if (checkIfHasLoopParent(annotatedElement)){
                            foundElements.add(annotatedElement);
                        }
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                noOfElementsScanned++;
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
                            || (classOfExpression.getQualifiedName().equalsIgnoreCase("com.google.android.gms.ads.BaseAdView")
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
    public boolean isAlreadyRefactored(PsiElement element){
        //TODO
        return false;
    }

    String a = new Integer(5).toString();

    @Override
    public void refactor(RefactoringCandidate candidate) {
        PsiElement element = candidate.getElement();
        DependencyCreator dependencyCreator = new DependencyCreator();
        ArrayList<PsiClass> dependencyClasses = dependencyCreator.createPackageAndFiles(element,dependencyFiles);
        PsiClass classOfTimer = JavaPsiFacade.getInstance(element.getProject()).findClass("energyRefactorings.BatteryAwareTimerTask",GlobalSearchScope.allScope(element.getProject()));
        if (classOfTimer == null) {
            classOfTimer = getClassFromList(dependencyClasses, "BatteryAwareTimerTask");
        }
        if (element instanceof PsiAnonymousClass && classOfTimer != null) {
            PsiClass elementClass = (PsiClass)((PsiAnonymousClass) element).getBaseClassReference().resolve();
            String className = elementClass.getQualifiedName();
            Collection<PsiReference> usages = Utilities.findUsages(element);
            switch (className) {
                case "java.util.TimerTask":
                    PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(element.getProject());
                    PsiIdentifier newMethodName = elementFactory.createIdentifier("runIfBatteryPermits");
                    PsiJavaCodeReferenceElement newReferenceElement = elementFactory.createClassReferenceElement(classOfTimer);
                    PsiMethod originalMethod = getMethodWithName(element, "run");
                    PsiMethodCallExpression originalPostDelayed = Utilities.findMethodReferenceInChildren(originalMethod,"android.os.Handler","postDelayed");
                    PsiElement delayTime = Utilities.getArgumentNo(Utilities.getArgumentsOfMethod(originalPostDelayed),2);
                    PsiElement newDelayTime = elementFactory.createExpressionFromText(delayTime.getText() + "*this.getSleepTimer()",delayTime);
                    PsiExpression args = elementFactory.createExpressionFromText(createCustomizationString(candidate.getBatteryAwarenessCriteria()),newReferenceElement);
                    PsiJavaCodeReferenceElement originalReferenceElement = getReferenceElement(element,"TimerTask");
                    PsiExpressionList originalArguments = getExpressionList(element);
                    if (originalMethod != null) {
                        WriteCommandAction.runWriteCommandAction(element.getProject(), new Runnable() {
                            @Override
                            public void run() {
                                for (PsiElement element : originalMethod.getChildren()) {
                                    if (element instanceof PsiIdentifier) {
                                        element.replace(newMethodName);
                                    }
                                }
                                originalReferenceElement.replace(newReferenceElement);
                                originalArguments.addAfter(args, originalArguments.getFirstChild());
                                originalArguments.addBefore(delayTime, originalArguments.getLastChild());
                                delayTime.replace(newDelayTime);
                            }
                        });
                        dependencyCreator.insertImportStatement(element, "BatteryAwareTimerTask", "energyRefactorings");
                        dependencyCreator.insertImportStatement(element, "BatteryAwarenessCriteria", "energyRefactorings");
                    } else {
                        System.out.println("cant find run method");
                    }
                    break;
            }
        }
    }

    private @Nullable PsiClass getClassFromList(ArrayList<PsiClass> dependencyClasses, String batteryAwareTimerTask) {
        for (PsiClass psiClass : dependencyClasses){
            if (psiClass.getName().equalsIgnoreCase("BatteryAwareTimerTask")){
                return psiClass;
            }
        }
        return null;
    }


    private boolean checkIfHasLoopParent(PsiElement element){
        while (element.getParent() != null) {
//            if (element instanceof PsiWhileStatement) {
//                refactoringCandidates.add(element);
//                return true;
//            } else
            if (element instanceof PsiMethod){
                if (visitAllUsages(element)) {
                    return true;
                }
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
                        refactoringCandidates.add(new RefactoringCandidate(element));
                        return true;
                    }
                }
            } else if (element instanceof PsiAnonymousClass){
                PsiClass elementClass = (PsiClass)((PsiAnonymousClass) element).getBaseClassReference().resolve();
                String className = elementClass.getQualifiedName();
                PsiType[] implementsListTypes = elementClass.getImplementsListTypes();
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
                    refactoringCandidates.add(new RefactoringCandidate(element));
                    return true;
                }
            }
            element = element.getParent();
        }
        return false;
    }

    private boolean visitAllUsages(PsiElement element) {
        Collection<PsiReference> usages = Utilities.findUsages(element);
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

    private @Nullable PsiMethod getMethodWithName(PsiElement element, String name){
        for (PsiElement child : element.getChildren()){
            if (child instanceof PsiMethod && ((PsiMethod) child).getName().equalsIgnoreCase(name)){
                return (PsiMethod)child;
            }
        }
        return null;
    }

    private @Nullable PsiJavaCodeReferenceElement getReferenceElement(PsiElement element, String name){
        for (PsiElement child : element.getChildren()){
            if (child instanceof PsiJavaCodeReferenceElement && ((PsiJavaCodeReferenceElement) child).getReferenceName().equalsIgnoreCase(name)){
                return (PsiJavaCodeReferenceElement) child;
            }
        }
        return null;
    }

    private @Nullable PsiExpressionList getExpressionList(PsiElement element){
        for (PsiElement child : element.getChildren()){
            if (child instanceof PsiExpressionList){
                return (PsiExpressionList) child;
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

    public String getEffectText(RefactoringCandidate candidate){
        BatteryAwarenessCriteria criteria = candidate.getBatteryAwarenessCriteria();
        StringBuilder result = new StringBuilder("The TimerTask used will be replaced by a wrapper-object of type BatteryAwareTimerTask.<br>" +
                "Before running the defined run()-method, it will always check if the selected power customizations allow it:<br>" +
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
        result.append(", updates will be slowed down linearly up to a slow factor of 10x</b>.<br>");
        result.append("If the device goes into power-saving mode, <b>the updates will ");
        if (!criteria.getSuspendIfInBatterySafeMode()){
            result.append("not ");
        }
        result.append("be reduced to once per hour</b>.<br>");
        result.append("If the battery level goes <b>below ");
        result.append(Integer.toString(criteria.getSuspendThreshold()));
        result.append("%, the updates will be reduced to once per hour</b>.<br>");
        return result.toString();
    }
}
