package DialogElements;

import Refactoring.BatteryAwarenessCriteria;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;

/*
 * SliderDemo.java requires all the files in the images/doggy
 * directory.
 */
public class CustomizationDialog extends JDialog
        implements
        WindowListener,
        ChangeListener,
        ItemListener {
    //Set up animation parameters.
    static final int POWER_SCHEME_LOW = 0;
    static final int POWER_SCHEME_MED = 1;
    static final int POWER_SCHEME_HIGH = 2;    //initial frames per second

    JCheckBox modeCheckbox;
    JSlider batterySlider;
    ComboBox powerSchemeCombobox;

    BatteryAwarenessCriteria criteria;

    private Hashtable<Integer, JComponent> powerSchemeNames;

    int powerScheme = 0;
    boolean suspend = false;

    public CustomizationDialog(JFrame owner, BatteryAwarenessCriteria criteria) {

        super(owner, Dialog.ModalityType.DOCUMENT_MODAL);

        this.criteria = criteria;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //Create the label.
        JLabel sliderLabel = new JBLabel("Select how aggressive power-saving should be:", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel modeLabel = new JBLabel("Check if process is to be suspended when device is in power saving mode");
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel suspendLabel = new JBLabel("Select battery level threshold below which process will be completely suspended");
        suspendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] powerSchemeNames = new String[]{"Low: only skip process when battery is below 25%",
                "Medium: start saving battery at 50%, continuously skipping process more often",
                "High: start at 75% battery to lower consumption. Below 25% process is only allowed once per hour"};

        //Create the slider.
        powerSchemeCombobox = new ComboBox(powerSchemeNames);
        powerSchemeCombobox.setSelectedIndex(criteria.getPowerSaveSchemeIndex());

        batterySlider = new JSlider(JSlider.HORIZONTAL,0,100,5);
        setSliderProperties(batterySlider);

        modeCheckbox = new JCheckBox();
        modeCheckbox.addItemListener(this);
        modeCheckbox.setSelected(criteria.getSuspendIfInBatterySafeMode());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCriteria();
                dispose();
            }
        });

        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS)
        );

        //Put everything together.
        getContentPane().add(sliderLabel);
        getContentPane().add(powerSchemeCombobox);
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));
        getContentPane().add(modeLabel);
        getContentPane().add(modeCheckbox);
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));
        getContentPane().add(suspendLabel);
        getContentPane().add(batterySlider);
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));
        getContentPane().add(okButton);
        this.pack();
    }

    private void setCriteria() {
        switch (powerSchemeCombobox.getSelectedIndex()){
            case 0:
                criteria.setPowerSafeScheme(BatteryAwarenessCriteria.PowerSaveScheme.POWER_SAFE_LOW);
                break;
            case 1:
                criteria.setPowerSafeScheme(BatteryAwarenessCriteria.PowerSaveScheme.POWER_SAFE_MEDIUM);
                break;
            case 2:
                criteria.setPowerSafeScheme(BatteryAwarenessCriteria.PowerSaveScheme.POWER_SAFE_HIGH);
                break;
        }
        criteria.setSuspendIfInBatterySafeMode(modeCheckbox.isSelected());
        criteria.setSuspendThreshold(batterySlider.getValue());
        System.out.println(criteria.toString());
    }

    private void setSliderProperties(JSlider slider) {
        slider.addChangeListener(this);

        //Turn on labels at major tick marks.

        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setSnapToTicks(true);
        slider.setValue(criteria.getSuspendThreshold());
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        slider.setFont(font);
    }

    /** Add a listener for window events. */
    void addWindowListener(Window w) {
        w.addWindowListener(this);
    }

    //React to window events.
    public void windowIconified(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            powerScheme = (int)source.getValue();
        }
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        //Now that we know which button was pushed, find out
        //whether it was selected or deselected.
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            suspend = false;
        } else {
            suspend = true;
        }
    }

    public static void main(String[] args) {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);


        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("SliderDemo");
                frame.getContentPane().add(new JLabel("parentFrame"));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                CustomizationDialog animator = new CustomizationDialog(frame, new BatteryAwarenessCriteria());
                animator.setVisible(true);
            }
        });
    }
}
