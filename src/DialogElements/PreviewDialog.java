package DialogElements;

import Transformation.*;

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
    private ArrayList<Transformation> allTransformations;
    private MainController callbackController;
    private JPanel controlPanel;
    private JPanel buttonPanel;

    public PreviewDialog(ArrayList<Transformation> transformations, MainController callbackController){
        this.callbackController = callbackController;
        this.allTransformations = transformations;
        frame = new JFrame("Energy Transformations");
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
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

                int numberOfElementsInGrid = 0;

                for (Transformation transformation : allTransformations) {
                    for (TransformationCandidate candidate : transformation.getTransformationCandidates()){
                        if (candidate.isSelected()){
                            GridBagConstraints c = new GridBagConstraints();
                            c.gridx = 0;
                            c.gridy = numberOfElementsInGrid;
                            controlPanel.add(new JLabel(candidate.getFileName() + ", line " + candidate.getCodeLineNumber() + ": "), c);
                            c.gridx = 1;
                            controlPanel.add(new JLabel("<html>" + transformation.getEffectText(candidate) + "</html>"),c);
                            numberOfElementsInGrid++;
                            c.gridx = 0;
                            c.gridy = numberOfElementsInGrid;
                            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                            c.fill = GridBagConstraints.HORIZONTAL;
                            c.weightx = 1;
                            c.gridwidth = GridBagConstraints.REMAINDER;
                            controlPanel.add(separator,c);
                            numberOfElementsInGrid++;
                        }
                    }
                }

                JButton okButton = new JButton("Perform Transformations");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        continueWithTransformation();
                    }
                });

                JButton cancelButton = new JButton("Back to Customization");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        backToCustomization();
                    }
                });

                frame.add(controlPanel);
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = numberOfElementsInGrid;
                controlPanel.add(buttonPanel, c);
                //        controlPanel.add(new JBScrollPane(listReferences),BorderLayout.LINE_END);
                buttonPanel.add(cancelButton, BorderLayout.WEST);
                buttonPanel.add(okButton, BorderLayout.EAST);
                if (numberOfElementsInGrid > 0) {
                    frame.pack();
                    frame.setVisible(true);
                }
            }
        });
    }

    public void continueWithTransformation(){
        frame.dispose();
        callbackController.performTransformations();
    }

    public void backToCustomization(){
        frame.dispose();
        callbackController.showCustomizationDialog();
    }
}
