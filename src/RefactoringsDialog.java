import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import CheckBoxList.*;
import com.intellij.ui.components.JBScrollPane;

/**
 * Created by pip on 03.02.2016.
 */
public class RefactoringsDialog {

    private JPanel controlPanel;
    private JPanel buttonPanel;
    private JFrame frame;
    private ArrayList<Refactoring> allRefactorings;
    private MainController callbackController;
    private HashMap<Refactoring,CheckboxListItem[]> refactoringWithSelectedItems;

    public RefactoringsDialog(ArrayList<Refactoring> refactorings, MainController callbackController){
        this.callbackController = callbackController;
        this.allRefactorings = refactorings;
        frame = new JFrame("Energy Refactorings");
        controlPanel = new JPanel();
        buttonPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.PAGE_AXIS));
        refactoringWithSelectedItems = new HashMap<Refactoring,CheckboxListItem[]>();
    }

    public void showDialog(){
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        int numberOfElementsFound = 0;

        for (Refactoring refactoring : allRefactorings) {
            if(refactoring.getFoundElements().size() > 0) {
                numberOfElementsFound += refactoring.getFoundElements().size();
                JPanel listPanel = new JPanel();
                JLabel listName = new JBLabel(refactoring.getName());
                Font font = listName.getFont();
                // same font but bold
                Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                listName.setFont(boldFont);
                //Number of elements the ELementVisitor searched
                JLabel elementsScanned = new JBLabel("Number of elements scanned: " + Integer.toString(refactoring.getNoOfElementsScanned()));

                listPanel.setLayout(new BorderLayout());
                listPanel.add(listName, BorderLayout.PAGE_START);
                listPanel.add(elementsScanned, BorderLayout.PAGE_END);

                // Create a list containing CheckboxListItem's
                ArrayList<String> codeLines = getCodeLines(refactoring);
                CheckboxListItem[] listItems = new CheckboxListItem[codeLines.size()];
                int index = 0;
                for (String line : codeLines) {
                    listItems[index] = new CheckboxListItem(line);
                    index++;
                }

                JBList list = new JBList(listItems);
                // Use a CheckboxListRenderer (see below)
                // to renderer list cells
                list.setCellRenderer(new CheckBoxListRenderer());
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//        ArrayList<String> classNames = getClassNames();
//        JBList listReferences = new JBList(classNames);

                // Add a mouse listener to handle changing selection

                list.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent event) {
                        JList<CheckboxListItem> list =
                                (JList<CheckboxListItem>) event.getSource();

                        // Get index of item clicked

                        int index = list.locationToIndex(event.getPoint());
                        CheckboxListItem item = (CheckboxListItem) list.getModel()
                                .getElementAt(index);

                        // Toggle selected state

                        item.setSelected(!item.isSelected());

                        // Repaint cell

                        list.repaint(list.getCellBounds(index, index));
                    }
                });
                listPanel.add(new JBScrollPane(list), BorderLayout.LINE_START);
                controlPanel.add(listPanel);
                JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                separator.setMaximumSize( new Dimension(Integer.MAX_VALUE, 1) );
                controlPanel.add(separator);
                refactoringWithSelectedItems.put(refactoring, listItems);
            }
        }

        JButton okButton = new JButton("Perform Refactorings");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                continueWithRefactoring();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });



        frame.add(controlPanel);
        controlPanel.add(buttonPanel);
//        controlPanel.add(new JBScrollPane(listReferences),BorderLayout.LINE_END);
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(okButton, BorderLayout.EAST);
        if (numberOfElementsFound > 0) {
            frame.pack();
            frame.setVisible(true);
        }
    }

    private ArrayList<String> getClassNames(Refactoring refactoring) {
        Iterator<PsiElement> iterator = refactoring.getFoundElements().iterator();
        ArrayList<String> classNames = new ArrayList<String>();
        while (iterator.hasNext()) {
            PsiElement element = iterator.next();
            Project project = element.getProject();
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(element.getContainingFile());
            int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
            String fileName = element.getContainingFile().getName();
            classNames.add(fileName + ":" + lineNum);
        }
        return classNames;
    }

    public ArrayList<String> getCodeLines(Refactoring refactoring){

        Iterator<PsiElement> iterator = refactoring.getFoundElements().iterator();
        ArrayList<String> codeLines = new ArrayList<String>();
        while (iterator.hasNext()) {
            PsiElement element = iterator.next();
            Project project = element.getProject();
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(element.getContainingFile());
            TextRange elementTextRange = element.getTextRange();
            int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
            String fileName = element.getContainingFile().getName();
            String line = fileName + ", line" + lineNum + ": " + document.getText(elementTextRange);
            codeLines.add(line);
        }
        return codeLines;
    }

    private void continueWithRefactoring(){
        for (Refactoring refactoring : refactoringWithSelectedItems.keySet()) {
            CheckboxListItem[] listItems = refactoringWithSelectedItems.get(refactoring);
            boolean[] elementsSelected = new boolean[listItems.length];
            int index = 0;
            for (CheckboxListItem item : listItems) {
                if (item.isSelected()) {
                    elementsSelected[index] = true;
                } else {
                    elementsSelected[index] = false;
                }
                index++;
            }
            frame.dispose();
            callbackController.performRefactorings(refactoring, elementsSelected);
        }
    }
}
