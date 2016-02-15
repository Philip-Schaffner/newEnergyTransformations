package DialogElements;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SharedListSelectionHandler implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
        StringBuilder output = new StringBuilder();
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

        boolean isAdjusting = e.getValueIsAdjusting();
        if (lsm.isSelectionEmpty()) {
            output.append(" <none>");
        } else {
            // Find out which indexes are selected.
            int selectedIndex = lsm.getLeadSelectionIndex();
        }
        System.out.println(output);
    }
}
