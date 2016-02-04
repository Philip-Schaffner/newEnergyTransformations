package CheckBoxList;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pip on 03.02.2016.
 */
public class CheckBoxListRenderer extends JCheckBox implements
        ListCellRenderer<CheckboxListItem> {

    @Override
    public Component getListCellRendererComponent(
            JList<? extends CheckboxListItem> list, CheckboxListItem value,
            int index, boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setSelected(value.isSelected());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setText(value.toString());
        return this;
    }
}
