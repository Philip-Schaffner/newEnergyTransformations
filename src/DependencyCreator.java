import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiImportStatementImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by pip on 31.01.2016.
 */
public class DependencyCreator {
    public void createPackageAndFiles(PsiElement element, String[] filesToCreate){
        VirtualFile fileOfElement = element.getContainingFile().getVirtualFile();
        Project project = element.getProject();
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                PsiFile androidManifest;
                int realFileFoundAt = -1;
                int index = 0;
                StringBuilder classText = new StringBuilder("");
                File resourceFile = new File(getClass().getClassLoader().getResource(filesToCreate[0]).getFile());
                try (Scanner scanner = new Scanner(resourceFile)) {

                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        classText.append(line).append("\n");
                    }

                    scanner.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Module module = ModuleUtil.findModuleForFile(fileOfElement, project);
                PsiManager psiManager = PsiManager.getInstance(project);
                PsiFile moduleFile = psiManager.findFile(LocalFileSystem.getInstance().findFileByPath(module.getModuleFilePath()));
                PsiDirectory moduleDir = (PsiDirectory) moduleFile.getParent();
                PsiFile[] androidManifestList = FilenameIndex.getFilesByName(project, "AndroidManifest.xml", moduleDir.getResolveScope());
                if (androidManifestList.length > 0) {
                    for (PsiFile file : androidManifestList){
                        if (file.getVirtualFile().getPath().contains("src/main")){
                            realFileFoundAt = index;
                        }
                        index++;
                    }
                    if (realFileFoundAt > -1) {
                        androidManifest = androidManifestList[realFileFoundAt];
                        PsiDirectory manifestDir = (PsiDirectory) androidManifest.getParent().findSubdirectory("java");
                        if (manifestDir.isDirectory() && ((PsiDirectory) manifestDir).findSubdirectory("energyRefactorings") == null) {
                            PsiDirectory newPackage = ((PsiDirectory) manifestDir).createSubdirectory("energyRefactorings");
                            PsiJavaFile newClassFile = (PsiJavaFile) newPackage.createFile("SleepTimeCalculator.java");
                            JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
                            PsiElementFactory factory = facade.getElementFactory();
                            PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
                            GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);
                            newClassFile.add(factory.createPackageStatement("energyRefactorings"));
                            newClassFile.add(factory.createImportStatement(facade.findClass("android.app.Application", globalSearchScope)));
                            newClassFile.add(factory.createImportStatement(facade.findClass("android.content.Intent", globalSearchScope)));
                            newClassFile.add(factory.createImportStatement(facade.findClass("android.content.IntentFilter", globalSearchScope)));
                            newClassFile.add(factory.createImportStatement(facade.findClass("android.os.BatteryManager", globalSearchScope)));
                            PsiClass nameTokensClass = factory.createClassFromText(classText.toString(), null).getInnerClasses()[0];
                            newClassFile.add(nameTokensClass);
                        }
                    }
                }
            }
        });
    }

    public void insertImportStatement(PsiElement usageElement, String className, String packageName) {
        String importText = packageName + "." + className;
        PsiJavaFile fileOfElement = (PsiJavaFile) usageElement.getContainingFile();
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(usageElement.getProject());
        PsiImportList importList = fileOfElement.getImportList();
        PsiImportStatementBase[] importStatements = importList.getAllImportStatements();
        if (!Utilities.checkIfImportExists(importStatements, importText)){
            PsiClass classToImport = elementFactory.createClass(className);
            classToImport.add(elementFactory.createPackageStatement(packageName));
            PsiElement importStatement = elementFactory.createImportStatement(classToImport);
            ((PsiImportStatementImpl) importStatement).getImportReference().replace(elementFactory.createFQClassNameReferenceElement(importText,GlobalSearchScope.allScope(usageElement.getProject())));
            WriteCommandAction.runWriteCommandAction(usageElement.getProject(),new Runnable() {
                @Override
                public void run() {
                    importList.add(importStatement);
                }
            });
        }

    }
}
