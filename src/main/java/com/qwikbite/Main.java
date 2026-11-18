package com.qwikbite;

import com.qwikbite.ui.QwikBiteApp;
import com.qwikbite.util.Style;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Style.setup();
        SwingUtilities.invokeLater(() -> new QwikBiteApp().setVisible(true));
    }
}
