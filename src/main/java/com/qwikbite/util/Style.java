package com.qwikbite.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public final class Style {
    // Campus Fresh palette: crisp mint, deep navy, energetic coral and sunshine yellow.
    public static final Color IVORY = new Color(244, 250, 247);
    public static final Color OLIVE = new Color(24, 50, 71);       // deep navy
    public static final Color TERRACOTTA = new Color(255, 107, 74); // coral
    public static final Color SAND = new Color(220, 243, 235);     // mint
    public static final Color INK = new Color(29, 41, 57);
    public static final Color MUTED = new Color(91, 107, 122);
    public static final Color TEAL = new Color(24, 169, 153);
    public static final Color SUNSHINE = new Color(255, 200, 87);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 19);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private Style() { }

    public static void setup() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) { /* Keep the platform look and feel if Nimbus is unavailable. */ }
        UIManager.put("defaultFont", BODY);
        UIManager.put("Label.font", BODY); UIManager.put("Button.font", BODY);
        UIManager.put("TextField.font", BODY); UIManager.put("PasswordField.font", BODY);
        UIManager.put("ComboBox.font", BODY); UIManager.put("Table.font", BODY);
        UIManager.put("OptionPane.messageFont", BODY); UIManager.put("OptionPane.buttonFont", BODY);
        UIManager.put("control", IVORY); UIManager.put("nimbusBase", OLIVE);
        UIManager.put("nimbusBlueGrey", SAND); UIManager.put("text", INK);
        UIManager.put("Panel.background", IVORY);
        UIManager.put("TabbedPane.selected", SAND);
        UIManager.put("Table.selectionBackground", new Color(190, 231, 220));
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("ComboBox.selectionBackground", TEAL);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    }
    public static JButton button(String text, boolean primary) {
        JButton b = new JButton(text); b.setFont(BODY.deriveFont(Font.BOLD, 13f));
        b.setForeground(primary ? Color.WHITE : OLIVE);
        b.setBackground(primary ? TERRACOTTA : SAND);
        b.setFocusPainted(false); b.setOpaque(true); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(primary ? TERRACOTTA : new Color(210,203,188),1,true), new EmptyBorder(10,17,10,17)));
        b.setMargin(new Insets(9,15,9,15));
        return b;
    }
    public static JPanel panel() { JPanel p = new JPanel(); p.setBackground(IVORY); return p; }
    public static JLabel heading(String text) { JLabel l = new JLabel(text); l.setFont(HEADING); l.setForeground(OLIVE); return l; }
    public static void field(JTextField f) { f.setBorder(new CompoundBorder(new LineBorder(new Color(205,198,183)), new EmptyBorder(9,10,9,10))); f.setBackground(Color.WHITE); f.setFont(BODY); }
}
