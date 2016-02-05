import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public class MainController extends AnAction{

    public ArrayList<Refactoring> allRefactorings;
    private boolean MAKE_CHANGES = true;

    public MainController(){
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        allRefactorings = new ArrayList<Refactoring>();
        allRefactorings.add(new HttpInLoopRefactoring());
        allRefactorings.add(new GpsUsageRefactoring());
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        ArrayList<VirtualFile> allJavaFilesInProject = FileGatherer.getAllJavaFilesInProject(project);
        for (Refactoring refactoring : allRefactorings){
            JavaRecursiveElementVisitor detector = refactoring.getDetector();
            for (VirtualFile file : allJavaFilesInProject){
                PsiManager.getInstance(project).findFile(file).accept(detector);
            }
        }
        RefactoringsDialog dialog = new RefactoringsDialog(allRefactorings,this);
        dialog.showDialog();
    }

    public void performRefactorings(Refactoring refactoring, boolean[] selecteElements){
        int index = 0;
        for (PsiElement element : refactoring.foundElements){
            if (selecteElements[index] &&  MAKE_CHANGES && !refactoring.isAlreadyRefactored(element)) {
                refactoring.refactor(element);
            }
            index++;
        }
    }
}
