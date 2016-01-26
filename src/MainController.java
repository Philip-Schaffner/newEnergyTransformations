import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public class MainController extends AnAction{

        @Override
        public void actionPerformed(AnActionEvent e) {
            final Project project = e.getProject();
            VirtualFile[] virtualFiles = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
            final Document document = e.getData(PlatformDataKeys.EDITOR).getDocument();
            if (project == null) {
                return;
            }
            PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
            ArrayList<VirtualFile> allJavaFilesInProject = FileGatherer.getAllJavaFilesInProject(project);
            ArrayList<PsiElement> foundElements = new ArrayList<PsiElement>();
            ArrayList<PsiElement> elementsToChange = new ArrayList<PsiElement>();
        }
}
