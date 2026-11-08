package bank.ui;

import bank.BankException;
import bank.dao.UserDAO;
import bank.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Online Banking - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        root.setBackground(UIUtil.BG);

        root.add(UIUtil.title("Welcome to Online Banking"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UIUtil.addRow(form, 0, "Email:", emailField);
        UIUtil.addRow(form, 1, "Password:", passField);
        root.add(form, BorderLayout.CENTER);

        JButton login = UIUtil.button("Login");
        JButton register = UIUtil.button("Create Account");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(login);
        buttons.add(register);
        root.add(buttons, BorderLayout.SOUTH);

        login.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegisterDialog(this).setVisible(true));

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pw = new String(passField.getPassword());
        if (email.isEmpty() || pw.isEmpty()) {
            UIUtil.error(this, "Please enter email and password.");
            return;
        }
        try {
            User u = userDAO.login(email, pw);
            if (u == null) {
                UIUtil.error(this, "Invalid email or password.");
                return;
            }
            dispose();
            if (u.isAdmin()) new AdminFrame(u).setVisible(true);
            else new DashboardFrame(u).setVisible(true);
        } catch (BankException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
