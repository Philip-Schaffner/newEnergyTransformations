package DialogElements;

import Refactoring.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by pip on 15.02.2016.
 */
public class PreviewDialog {

    private JFrame frame;
    private ArrayList<Refactoring> allRefactorings;
    private MainController callbackController;
    private JPanel controlPanel;
    private JPanel buttonPanel;

    public PreviewDialog(ArrayList<Refactoring> refactorings, MainController callbackController){
        this.callbackController = callbackController;
        this.allRefactorings = refactorings;
        frame = new JFrame("Energy Refactorings");
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

                int numberOfElementsToPreview = 0;

                for (Refactoring refactoring : allRefactorings) {
                    for (RefactoringCandidate candidate : refactoring.getRefactoringCandidates()){
                        if (candidate.isSelected()){
                            controlPanel.add(new JLabel("<html>" + candidate.getFileName() + ", line " + candidate.getCodeLineNumber() + ": " + refactoring.getEffectText(candidate) + "</html>"));
                            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                            controlPanel.add(separator);
                            numberOfElementsToPreview++;
                        }
                    }
                }

                JButton okButton = new JButton("Perform Refactorings");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        continueWithRefactoring();
                    }
                });

                JButton cancelButton = new JButton("Back to Customization");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        backToCustomization();
                    }
                });

                frame.add(controlPanel);
                controlPanel.add(buttonPanel);
                //        controlPanel.add(new JBScrollPane(listReferences),BorderLayout.LINE_END);
                buttonPanel.add(cancelButton, BorderLayout.WEST);
                buttonPanel.add(okButton, BorderLayout.EAST);
                if (numberOfElementsToPreview > 0) {
                    frame.pack();
                    frame.setVisible(true);
                }
            }
        });
    }

    public void continueWithRefactoring(){
        frame.dispose();
        callbackController.performRefactorings();
    }

    public void backToCustomization(){
        frame.dispose();
        callbackController.showCustomizationDialog();
    }
}
