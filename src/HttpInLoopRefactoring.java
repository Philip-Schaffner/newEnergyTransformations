import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by pip on 26.01.2016.
 */
public class HttpInLoopRefactoring extends Refactoring {

    public HttpInLoopRefactoring(){
        super();
        this.name = "HTTP calls inside a loop";
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
    public boolean isAlreadyRefactored(PsiElement element){
        return false;
    }

    @Override
    public void refactor(PsiElement element) {
        DependencyCreator dependencyCreator = new DependencyCreator();
        String[] filesToCreate = new String[]{"SleepTimeCalculator.java"};
        dependencyCreator.createPackageAndFiles(element,filesToCreate);
        if(element instanceof PsiMethodCallExpression){
            dependencyCreator.insertImportStatement(element, "SleepTimeCalculator", "energyRefactorings");
            surroundWithCheck(element);
        }
        else if(element instanceof PsiMethod){
            Collection<PsiReference> usages = Utilities.findUsages(element);
            for (PsiReference ref : usages){
                if (ref instanceof PsiReferenceExpression) {
                    refactor(((PsiReferenceExpression) ref).getParent());
                }
            }
        }
        if (element instanceof PsiLocalVariable) {
            PsiType psiType = ((PsiLocalVariable) element).getType();
            if (psiType instanceof PsiClassType) {
                String className = ((PsiClassType) psiType).resolve().getQualifiedName();
                Collection<PsiReference> usages = Utilities.findUsages(element);
                switch (className) {
                    case "java.util.TimerTask":
                        for (PsiReference usage : usages) {
                            PsiElement usageElement = usage.getElement();
                            while (!(usageElement == null
                                    || (usageElement instanceof PsiMethodCallExpression))) {
                                usageElement = usageElement.getParent();
                            }
                            replaceTimer(usageElement);
                            dependencyCreator.insertImportStatement(usageElement, "SleepTimeCalculator", "energyRefactorings");
                        }
                        break;
                    case "android.os.AsyncTask":
                        for (PsiReference usage : usages) {
                            PsiElement usageElement = usage.getElement();
                            while (!(usageElement == null
                                    || (usageElement instanceof PsiMethodCallExpression))) {
                                usageElement = usageElement.getParent();
                            }
                            if (usageElement instanceof PsiMethodCallExpression
                                    && usageElement.getText().contains("schedule")) {
                                PsiLiteralExpression timer = (PsiLiteralExpression) Utilities.findInChildren(usageElement, PsiLiteralExpressionImpl.class);
                                PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(usageElement.getProject());
                                PsiElement newTimer = elementFactory.createExpressionFromText("SleepTimeCalculator.getSleepTimer()", usageElement);
                                PsiElement importStatement = elementFactory.createImportStatement(elementFactory.createClass("SleepTimeCalculator"));
                                PsiJavaFile timerFile = (PsiJavaFile) timer.getContainingFile();
                                PsiImportStatementBase[] importList = timerFile.getImportList().getAllImportStatements();
                                PsiImportStatementBase lastImport = importList[importList.length - 1];
                                WriteCommandAction.runWriteCommandAction(usageElement.getProject(), new Runnable() {
                                    @Override
                                    public void run() {
                                        timer.replace(newTimer);
                                        timerFile.addAfter(lastImport, importStatement);
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        }
    }

    private void surroundWithCheck(PsiElement element) {
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(element.getProject());
        String conditionText = "SleepTimeCalculator.getInstance().canIRunAgain(" + element.hashCode() + ")";
        String statementText = "if (condition) {methodcall}";
        String statementText2 = ";";
        PsiElement statementElement = elementFactory.createStatementFromText(statementText, null);
        PsiElement conditionElement = elementFactory.createExpressionFromText(conditionText,element);
        statementElement.getChildren()[3].replace(conditionElement);
        statementElement.getChildren()[6].getChildren()[0].getChildren()[1].replace(element.getParent());
        WriteCommandAction.runWriteCommandAction(element.getProject(), new Runnable() {
            @Override
            public void run() {
                element.getParent().replace(statementElement);
            }
        });
    }

    private void replaceTimer(PsiElement usageElement) {
        if (usageElement instanceof PsiMethodCallExpression
                && usageElement.getText().contains("schedule")) {
            PsiExpression[] arguments = ((PsiMethodCallExpression) usageElement).getArgumentList().getExpressions();
            PsiExpression timer = arguments[1];
            PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(usageElement.getProject());
            PsiJavaFile timerFile = (PsiJavaFile) timer.getContainingFile();
            if (!timer.getText().contains("getSleepTimer")) {
                PsiElement newTimer = elementFactory.createExpressionFromText(timer.getText() + "*SleepTimeCalculator.getSleepTimer()", usageElement);
                PsiComment comment = elementFactory.createCommentFromText("//refactored by EnergyRefactorings ", null);
                WriteCommandAction.runWriteCommandAction(usageElement.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        PsiElement anchor = timer.replace(newTimer);
                        timerFile.addBefore(comment, anchor);
                    }
                });
            }
        }
    }


    private boolean checkIfHasLoopParent(PsiElement element){
        while (element.getParent() != null) {
            if (element instanceof PsiWhileStatement) {
                elementsToRefactor.add(element);
                return true;
            } else if (element instanceof PsiMethod){
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
                        elementsToRefactor.add(element);
                        return true;
                    }
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
}
