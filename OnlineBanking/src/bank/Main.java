package bank;

import bank.dao.UserDAO;
import bank.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            try {
                new UserDAO().ensureAdmin();
            } catch (BankException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the database.\nCheck db.properties and that MySQL is running.\n\n"
                                + e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
