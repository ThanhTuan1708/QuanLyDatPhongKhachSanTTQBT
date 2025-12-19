package event;


import ui.gui.GUI_NhanVienLeTan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EventDashBoardLeTan {

    public static void addStatBoxClickEvent(
            JPanel box,
            String type,
            Runnable callback
    ) {
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        box.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if ("checkin".equalsIgnoreCase(type)) {
                    GUI_NhanVienLeTan.GUI_CheckIn gui =
                            new GUI_NhanVienLeTan.GUI_CheckIn(callback);
                    gui.setVisible(true);

                } else if ("checkout".equalsIgnoreCase(type)) {
                    GUI_NhanVienLeTan.GUI_CheckOut gui =
                            new GUI_NhanVienLeTan.GUI_CheckOut(callback);
                    gui.setVisible(true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                box.setBackground(box.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                box.setBackground(box.getBackground().brighter());
            }
        });
    }
}
