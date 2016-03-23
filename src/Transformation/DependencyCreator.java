package Transformation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiImportStatementImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by pip on 31.01.2016.
 */
public class DependencyCreator {
    public ArrayList<PsiClass> createPackageAndFiles(PsiElement element, String[] filesToCreate){
        ArrayList<PsiClass> psiClasses = new ArrayList<>();
        VirtualFile fileOfElement = element.getContainingFile().getVirtualFile();
        Project project = element.getProject();
        for (String file : filesToCreate) {
            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                @Override
                public void run() {
                    String classText = getClassText(file);
                    PsiDirectory rootDir = getProjectRoot(element);
                    PsiDirectory newDirectory;
                    if (rootDir.isDirectory() && ((PsiDirectory) rootDir).findSubdirectory("energyTransformations") == null) {
                        newDirectory = ((PsiDirectory) rootDir).createSubdirectory("energyTransformations");
                    }else {
                        newDirectory = ((PsiDirectory) rootDir).findSubdirectory("energyTransformations");
                    }
                    if (newDirectory.findFile(file) == null) {
                        PsiJavaFile newClassFile = (PsiJavaFile) newDirectory.createFile(file);
                        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
                        PsiElementFactory factory = facade.getElementFactory();
                        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
                        GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);
                        newClassFile.add(factory.createPackageStatement("energyTransformations"));
                        for (String importStatement : getImportStatements(file)) {
                            newClassFile.add(factory.createImportStatement(facade.findClass(importStatement, globalSearchScope)));
                        }
                        PsiClass nameTokensClass = factory.createClassFromText(classText.toString(), null).getInnerClasses()[0];
                        newClassFile.add(nameTokensClass);
                        psiClasses.add(nameTokensClass);
                    }
                }
            });
        }
        return psiClasses;
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

    private @Nullable PsiDirectory getProjectRoot(PsiElement element){
        Project myProject = element.getProject();

        final List<VirtualFile> sourceRoots = new ArrayList<VirtualFile>();
        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(myProject);
        ContainerUtil.addAll(sourceRoots, projectRootManager.getContentSourceRoots());

        final PsiManager psiManager = PsiManager.getInstance(myProject);
        final Set<PsiPackage> topLevelPackages = new HashSet<PsiPackage>();

        for (final VirtualFile root : sourceRoots) {
            final PsiDirectory directory = psiManager.findDirectory(root);
            if (directory == null) {
                continue;
            } else if (FileGatherer.getAllJavaFilesInDir(directory.getVirtualFile()).contains(element.getContainingFile().getVirtualFile()))
                return directory;
        }
        return null;
    }

    private String getClassText(String filename){
        StringBuilder classText = new StringBuilder("");
        try {
            URL resourceFileURL = getClass().getClassLoader().getResource(filename);
            Scanner scanner = new Scanner(resourceFileURL.openStream());

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                classText.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return classText.toString();
    }

    private ArrayList<String> getImportStatements(String filename){
        ArrayList<String> imports = new ArrayList<>();
        try {
            URL resourceFileURL = getClass().getClassLoader().getResource(filename);
            Scanner scanner = new Scanner(resourceFileURL.openStream());

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(" |;");
                if (tokens.length > 1) {
                    if (tokens[0].equalsIgnoreCase("import")) {
                        imports.add(tokens[1]);
                    }
                }
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imports;
    }
}

