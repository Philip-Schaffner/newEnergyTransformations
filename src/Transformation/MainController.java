package Transformation;

import DialogElements.PreviewDialog;
import DialogElements.TransformationsDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;

/**
 * Created by pip on 26.01.2016.
 */
public class MainController extends AnAction{

    public ArrayList<Transformation> allTransformations;
    private boolean MAKE_CHANGES = true;
    private double runtimeLastAnalysis = 0;

    public MainController(){
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        long startTime = System.nanoTime();
        allTransformations = new ArrayList<Transformation>();
        allTransformations.add(new HttpInLoopTransformation());
        allTransformations.add(new GpsUsageTransformation());
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        ArrayList<VirtualFile> allJavaFilesInProject = FileGatherer.getAllJavaFilesInProject(project);
        for (Transformation transformation : allTransformations){
            JavaRecursiveElementVisitor detector = transformation.getDetector();
            for (VirtualFile file : allJavaFilesInProject){
                PsiManager.getInstance(project).findFile(file).accept(detector);
            }
        }
        runtimeLastAnalysis = (double)(System.nanoTime() - startTime)/ 1000000000.0;
        showCustomizationDialog();
    }

    public void previewTransformations(){
        PreviewDialog previewDialog = new PreviewDialog(allTransformations, this);
        previewDialog.showDialog();
    }

    public void showCustomizationDialog(){
        TransformationsDialog dialog = new TransformationsDialog(allTransformations,this);
        dialog.showDialog();
    }

    public void performTransformations(){
        int index = 0;
        for (Transformation transformation : allTransformations) {
            for (TransformationCandidate candidate : transformation.transformationCandidates) {
                if (candidate.isSelected() && MAKE_CHANGES && !transformation.isAlreadyTransformed(candidate.getElement())) {
                    transformation.transform(candidate);
//                ((ProjectManagerImpl) ProjectManager.getInstance()).reloadProject(element.getProject());
                }
                index++;
            }
        }
    }

    public double getRuntimeLastAnalysis(){
        return runtimeLastAnalysis;
    }
}
