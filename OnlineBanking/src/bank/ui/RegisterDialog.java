package bank.ui;

import bank.BankException;
import bank.dao.UserDAO;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final JTextField name = new JTextField(20);
    private final JTextField email = new JTextField(20);
    private final JTextField phone = new JTextField(20);
    private final JPasswordField pass = new JPasswordField(20);
    private final JPasswordField confirm = new JPasswordField(20);
    private final JComboBox<String> type = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
    private final UserDAO userDAO = new UserDAO();

    public RegisterDialog(Frame owner) {
        super(owner, "Create Account", true);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel(new GridBagLayout());
        UIUtil.addRow(form, 0, "Full name:", name);
        UIUtil.addRow(form, 1, "Email:", email);
        UIUtil.addRow(form, 2, "Phone (10 digits):", phone);
        UIUtil.addRow(form, 3, "Account type:", type);
        UIUtil.addRow(form, 4, "Password (min 6):", pass);
        UIUtil.addRow(form, 5, "Confirm password:", confirm);
        root.add(form, BorderLayout.CENTER);

        JButton submit = UIUtil.button("Register");
        JPanel south = new JPanel();
        south.add(submit);
        root.add(south, BorderLayout.SOUTH);
        submit.addActionListener(e -> submit());

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private void submit() {
        String n = name.getText().trim(), em = email.getText().trim(), ph = phone.getText().trim();
        String pw = new String(pass.getPassword()), pw2 = new String(confirm.getPassword());

        if (n.isEmpty()) { UIUtil.error(this, "Name is required."); return; }
        if (!em.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) { UIUtil.error(this, "Enter a valid email."); return; }
        if (!ph.matches("\\d{10}")) { UIUtil.error(this, "Phone must be exactly 10 digits."); return; }
        if (pw.length() < 6) { UIUtil.error(this, "Password must be at least 6 characters."); return; }
        if (!pw.equals(pw2)) { UIUtil.error(this, "Passwords do not match."); return; }

        try {
            long accNo = userDAO.register(n, em, ph, pw, (String) type.getSelectedItem());
            UIUtil.info(this, "Account created!\nYour account number is " + accNo + ".\nYou can now log in.");
            dispose();
        } catch (BankException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
