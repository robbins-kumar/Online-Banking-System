package bank.ui;

import bank.BankException;
import bank.dao.AccountDAO;
import bank.dao.TransactionDAO;
import bank.dao.UserDAO;
import bank.model.Account;
import bank.model.Transaction;
import bank.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class DashboardFrame extends JFrame {
    private final User user;
    private Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO txDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();

    private final JLabel accLabel = new JLabel();
    private final JLabel balanceLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Txn ID", "Date", "Type", "Amount", "Other Account", "Balance After"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public DashboardFrame(User user) {
        super("Online Banking - " + user.getName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Deposit / Withdraw", buildCashPanel());
        tabs.addTab("Transfer", buildTransferPanel());
        tabs.addTab("History", buildHistoryPanel());
        tabs.addTab("Settings", buildSettingsPanel());
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtil.PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel hello = new JLabel("Hello, " + user.getName());
        hello.setForeground(Color.WHITE);
        hello.setFont(new Font("SansSerif", Font.BOLD, 18));

        for (JLabel l : new JLabel[]{accLabel, statusLabel}) {
            l.setForeground(Color.WHITE);
            l.setFont(UIUtil.BASE);
        }
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel left = new JPanel(new GridLayout(3, 1));
        left.setOpaque(false);
        left.add(hello);
        left.add(accLabel);
        left.add(statusLabel);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 6));
        right.setOpaque(false);
        right.add(balanceLabel);
        right.add(logout);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCashPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        JTextField amount = new JTextField(14);
        UIUtil.addRow(p, 0, "Amount (Rs.):", amount);

        JButton dep = UIUtil.button("Deposit");
        JButton wd = UIUtil.button("Withdraw");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.add(dep);
        btns.add(wd);
        UIUtil.addRow(p, 1, "", btns);

        dep.addActionListener(e -> {
            try {
                accountDAO.deposit(account.getAccountNo(), UIUtil.parseAmount(amount.getText()));
                amount.setText("");
                refresh();
                UIUtil.info(this, "Deposit successful.");
            } catch (BankException ex) { UIUtil.error(this, ex.getMessage()); }
        });
        wd.addActionListener(e -> {
            try {
                accountDAO.withdraw(account.getAccountNo(), UIUtil.parseAmount(amount.getText()));
                amount.setText("");
                refresh();
                UIUtil.info(this, "Withdrawal successful.");
            } catch (BankException ex) { UIUtil.error(this, ex.getMessage()); }
        });
        return p;
    }

    private JPanel buildTransferPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        JTextField to = new JTextField(14);
        JTextField amount = new JTextField(14);
        UIUtil.addRow(p, 0, "To account no.:", to);
        UIUtil.addRow(p, 1, "Amount (Rs.):", amount);
        JButton send = UIUtil.button("Transfer");
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btn.add(send);
        UIUtil.addRow(p, 2, "", btn);

        send.addActionListener(e -> {
            try {
                long dest;
                try { dest = Long.parseLong(to.getText().trim()); }
                catch (NumberFormatException ex) { throw new BankException("Enter a valid account number."); }
                BigDecimal amt = UIUtil.parseAmount(amount.getText());
                int ok = JOptionPane.showConfirmDialog(this,
                        "Send " + UIUtil.money(amt) + " to account " + dest + "?", "Confirm transfer",
                        JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
                accountDAO.transfer(account.getAccountNo(), dest, amt);
                to.setText("");
                amount.setText("");
                refresh();
                UIUtil.info(this, "Transfer successful.");
            } catch (BankException ex) { UIUtil.error(this, ex.getMessage()); }
        });
        return p;
    }

    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTable table = new JTable(historyModel);
        table.setRowHeight(24);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton reload = UIUtil.button("Refresh");
        reload.addActionListener(e -> refresh());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(reload);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildSettingsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        JPasswordField oldPw = new JPasswordField(16);
        JPasswordField newPw = new JPasswordField(16);
        JPasswordField confirm = new JPasswordField(16);
        UIUtil.addRow(p, 0, "Current password:", oldPw);
        UIUtil.addRow(p, 1, "New password:", newPw);
        UIUtil.addRow(p, 2, "Confirm new:", confirm);
        JButton save = UIUtil.button("Change Password");
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btn.add(save);
        UIUtil.addRow(p, 3, "", btn);

        save.addActionListener(e -> {
            String np = new String(newPw.getPassword());
            if (np.length() < 6) { UIUtil.error(this, "New password must be at least 6 characters."); return; }
            if (!np.equals(new String(confirm.getPassword()))) { UIUtil.error(this, "Passwords do not match."); return; }
            try {
                userDAO.changePassword(user.getId(), new String(oldPw.getPassword()), np);
                oldPw.setText(""); newPw.setText(""); confirm.setText("");
                UIUtil.info(this, "Password changed.");
            } catch (BankException ex) { UIUtil.error(this, ex.getMessage()); }
        });
        return p;
    }

    /** Reloads balance, status and history from the database. */
    private void refresh() {
        try {
            account = accountDAO.findByUser(user.getId());
            accLabel.setText("Account: " + account.getAccountNo() + "  (" + account.getType() + ")");
            statusLabel.setText("Status: " + account.getStatus());
            balanceLabel.setText(UIUtil.money(account.getBalance()));

            historyModel.setRowCount(0);
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy HH:mm");
            for (Transaction t : txDAO.history(account.getAccountNo(), 100)) {
                historyModel.addRow(new Object[]{t.getId(), fmt.format(t.getCreatedAt()), t.getType(),
                        UIUtil.money(t.getAmount()), t.getOtherAccount() == null ? "-" : t.getOtherAccount(),
                        UIUtil.money(t.getBalanceAfter())});
            }
        } catch (BankException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
