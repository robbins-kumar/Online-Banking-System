package bank.ui;

import bank.BankException;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class UIUtil {
    public static final Color PRIMARY = new Color(0x1F4E79);
    public static final Color BG = new Color(0xF4F6F8);
    public static final Font BASE = new Font("SansSerif", Font.PLAIN, 14);

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(BASE.deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        return b;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(PRIMARY);
        return l;
    }

    /** Adds "label  [field]" as one row of a GridBagLayout form. */
    public static void addRow(JPanel p, int row, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.gridy = row;
        g.gridx = 0; g.anchor = GridBagConstraints.EAST;
        JLabel l = new JLabel(label);
        l.setFont(BASE);
        p.add(l, g);
        g.gridx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(field, g);
    }

    public static BigDecimal parseAmount(String text) throws BankException {
        try {
            BigDecimal v = new BigDecimal(text.trim());
            if (v.signum() <= 0) throw new BankException("Amount must be greater than zero.");
            if (v.scale() > 2) throw new BankException("Amount can have at most 2 decimal places.");
            return v.setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException e) {
            throw new BankException("Please enter a valid amount.");
        }
    }

    public static String money(BigDecimal v) {
        return String.format("Rs. %,.2f", v);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
