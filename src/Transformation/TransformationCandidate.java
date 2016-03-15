package Transformation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;

/**
 * Created by pip on 10.02.2016.
 */
public class TransformationCandidate {

    private PsiElement element;
    private boolean isSelected;
    private BatteryAwarenessCriteria batteryAwarenessCriteria;

    public PsiElement getElement() {
        return element;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public BatteryAwarenessCriteria getBatteryAwarenessCriteria() {
        return batteryAwarenessCriteria;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setBatteryAwarenessCriteria(BatteryAwarenessCriteria batteryAwarenessCriteria) {
        this.batteryAwarenessCriteria = batteryAwarenessCriteria;
    }

    public TransformationCandidate(PsiElement element){
        this.element = element;
        isSelected = false;
        batteryAwarenessCriteria = new BatteryAwarenessCriteria();
    }

    public String getCodeLineText(){
        Project project = element.getProject();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = documentManager.getDocument(element.getContainingFile());
        TextRange elementTextRange = element.getTextRange();
        String line = new String(document.getText(elementTextRange));
        return line;
    }

    public String getFileName(){
        String fileName = element.getContainingFile().getName();
        return fileName;
    }

    public int getCodeLineNumber(){
        Project project = element.getProject();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = documentManager.getDocument(element.getContainingFile());
        int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
        return lineNum;
    }
}
