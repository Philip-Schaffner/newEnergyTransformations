package DialogElements;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import Transformation.*;

/**
 * Created by pip on 03.02.2016.
 */
public class TransformationsDialog {

    private JFrame frame;
    private ArrayList<Transformation> allTransformations;
    private MainController callbackController;
    private JPanel controlPanel;
    private JPanel buttonPanel;

    public TransformationsDialog(ArrayList<Transformation> transformations, MainController callbackController){
        this.callbackController = callbackController;
        this.allTransformations = transformations;
        frame = new JFrame("Energy Transformations");
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.PAGE_AXIS));
        buttonPanel = new JPanel();
    }

    public void showDialog(){
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                int numberOfElementsToTransform = 0;

                for (Transformation transformation : allTransformations) {

                    JPanel listPanel = new JPanel();
                    JLabel listName = new JBLabel(transformation.getName());
                    Font font = listName.getFont();
                    // same font but bold
                    Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                    listName.setFont(boldFont);
                    JLabel elementsScanned = new JBLabel("Number of elements scanned: " + Integer.toString(transformation.getNoOfElementsScanned()));
                    listPanel.setLayout(new BorderLayout());
                    listPanel.add(listName, BorderLayout.PAGE_START);
                    listPanel.add(elementsScanned, BorderLayout.PAGE_END);
                    controlPanel.add(listPanel);

                    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    controlPanel.add(separator);

                    if (transformation.getTransformationCandidates().size() > 0) {
                        numberOfElementsToTransform = transformation.getTransformationCandidates().size();
                        //Number of elements the ELementVisitor searched
                        int index = 0;

                        TransformationsTableModel model = new TransformationsTableModel(frame);

                        for (TransformationCandidate candidate : transformation.getTransformationCandidates()) {
                            model.addCandidate(candidate);
                        }

                        JBTable table = new JBTable(model);
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        ListSelectionModel listSelectionModel = table.getSelectionModel();
                        listSelectionModel.addListSelectionListener(
                                new SharedListSelectionHandler());

                        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                        table.doLayout();

                        listPanel.add(new JBScrollPane(table), BorderLayout.LINE_START);
                    } else {
                        JLabel nothingFound = new JBLabel("Analysis of the code returned no hits for the searched patterns");
                        listPanel.add(nothingFound);
                    }
                }

                JLabel runTimeOfAnalysis = new JBLabel("Runtime of analysis in seconds: " + Double.toString(callbackController.getRuntimeLastAnalysis()));
                controlPanel.add(runTimeOfAnalysis);

                JButton okButton = new JButton("Preview Changes");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        continueWithTransformation();
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
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    private ArrayList<String> getClassNames(Transformation transformation) {
        Iterator<TransformationCandidate> iterator = transformation.getTransformationCandidates().iterator();
        ArrayList<String> classNames = new ArrayList<String>();
        while (iterator.hasNext()) {
            PsiElement element = iterator.next().getElement();
            Project project = element.getProject();
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(element.getContainingFile());
            int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
            String fileName = element.getContainingFile().getName();
            classNames.add(fileName + ":" + lineNum);
        }
        return classNames;
    }

    private void continueWithTransformation(){
        frame.dispose();
        callbackController.previewTransformations();
    }
}
