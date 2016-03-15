package DialogElements;

import Refactoring.BatteryAwarenessCriteria;
import Refactoring.TransformationCandidate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by pip on 09.02.2016.
 */
public class TransformationsTableModel extends DefaultTableModel {


    JFrame frame;
    ArrayList<TransformationCandidate> transformationCandidates;

    public TransformationsTableModel(JFrame frame){
        super(new String[]{"Refactor", "Class", "Line", "Code", "Customization"}, 0);
        this.frame = frame;
        this.transformationCandidates = new ArrayList<>();
    }

    public void addCandidate(TransformationCandidate candidate){
        this.addRow(new Object[]{candidate.isSelected(),candidate.getFileName(),candidate.getCodeLineNumber(),candidate.getCodeLineText(),candidate.getBatteryAwarenessCriteria()});
        transformationCandidates.add(candidate);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class clazz = String.class;
        switch (columnIndex) {
            case 0:
                clazz = Boolean.class;
                break;
            case 2:
                clazz = Integer.class;
                break;
        }
        return clazz;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (aValue instanceof Boolean && column == 0) {
            System.out.println(aValue);
            Vector rowData = (Vector)getDataVector().get(row);
            rowData.set(0, (boolean)aValue);
            fireTableCellUpdated(row, column);
            transformationCandidates.get(row).setSelected((Boolean)aValue);
            if ((Boolean)aValue){
                CustomizationDialog customizationDialog = new CustomizationDialog(frame, (BatteryAwarenessCriteria)rowData.get(4));
                customizationDialog.setVisible(true);
            }
            System.out.println(((BatteryAwarenessCriteria)rowData.get(4)).toString());
        }
    }

}
