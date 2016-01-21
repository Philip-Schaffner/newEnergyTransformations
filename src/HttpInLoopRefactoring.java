import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * Created by pip on 12.01.2016.
 */
public class HttpInLoopRefactoring {

    Project project;
    VirtualFile file;

    private String classText =
            " * Created by pip on 12.01.2016.\n" +
            " */\n" +
            "public class SleepTimeCalculator {\n" +
            "\n" +
            "    public static Application getApplicationUsingReflection() throws Exception {\n" +
            "        return (Application) Class.forName(\"android.app.ActivityThread\")\n" +
            "                .getMethod(\"currentApplication\").invoke(null, (Object[]) null);\n" +
            "    }\n" +
            "\n" +
            "    public float getSleepTimer(){\n" +
            "        try{\n" +
            "            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);\n" +
            "            Intent batteryStatus = getApplicationUsingReflection().registerReceiver(null, ifilter);\n" +
            "            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);\n" +
            "            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);\n" +
            "            float batteryPct = level / (float)scale;\n" +
            "            return batteryPct;\n" +
            "        } catch (Exception e){\n" +
            "            System.out.println(e);\n" +
            "            return 100;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    public HttpInLoopRefactoring(Project project, VirtualFile file){
        this.file = file;
        this.project = project;
    }

//    public boolean createDependencies(){
//        final PsiDirectory[] createdNameTokensPackageDirectories = createdNameTokensPackage.getDirectories();
//        final PsiFile element = PsiFileFactory.getInstance(this.project).createFileFromText("SleepTimeCalculator", JavaFileType.INSTANCE,"../CodeToImport/SleepTimeCalculator.java");
//        ApplicationManager.getApplication().runWriteAction(new Runnable() {
//            public void run() {
//                PsiElement createdNameTokensClass = createdNameTokensPackageDirectories[0].add(element);
//                PsiJavaFile javaFile = (PsiJavaFile) createdNameTokensClass;
//                PsiClass[] clazzes = javaFile.getClasses();
//            }
//        });
//        return false;
//    }

    public void createFile(){

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                PsiFile androidManifest;
                int realFileFoundAt = -1;
                int index = 0;
                Module module = ModuleUtil.findModuleForFile(file, project);
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
                            PsiClass nameTokensClass = factory.createClassFromText(classText, null).getInnerClasses()[0];
                            newClassFile.add(nameTokensClass);
                        }
                    }
                }
            }
        });
    }
}
