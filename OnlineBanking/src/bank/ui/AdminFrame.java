package bank.ui;

import bank.BankException;
import bank.dao.AccountDAO;
import bank.dao.TransactionDAO;
import bank.model.Transaction;
import bank.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminFrame extends JFrame {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO txDAO = new TransactionDAO();

    private final DefaultTableModel accModel = new DefaultTableModel(
            new String[]{"Account No", "Name", "Email", "Phone", "Type", "Balance", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel txModel = new DefaultTableModel(
            new String[]{"Txn ID", "Date", "Account", "Type", "Amount", "Other Account"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable accTable = new JTable(accModel);
    private final JLabel summary = new JLabel();

    public AdminFrame(User admin) {
        super("Online Banking - Admin Panel (" + admin.getName() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 540);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel t = new JLabel("Admin Panel");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("SansSerif", Font.BOLD, 20));
        summary.setForeground(Color.WHITE);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        header.add(t, BorderLayout.WEST);
        header.add(summary, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer Accounts", buildAccountsTab());
        tabs.addTab("All Transactions", buildTxTab());
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildAccountsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        accTable.setRowHeight(24);
        accTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(accTable), BorderLayout.CENTER);

        JButton freeze = UIUtil.button("Freeze Account");
        JButton unfreeze = UIUtil.button("Unfreeze Account");
        JButton reload = UIUtil.button("Refresh");
        freeze.addActionListener(e -> changeStatus("FROZEN"));
        unfreeze.addActionListener(e -> changeStatus("ACTIVE"));
        reload.addActionListener(e -> refresh());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        south.add(freeze);
        south.add(unfreeze);
        south.add(reload);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTxTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTable table = new JTable(txModel);
        table.setRowHeight(24);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void changeStatus(String status) {
        int row = accTable.getSelectedRow();
        if (row < 0) { UIUtil.error(this, "Select an account first."); return; }
        long accNo = (Long) accModel.getValueAt(row, 0);
        try {
            accountDAO.setStatus(accNo, status);
            refresh();
        } catch (BankException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void refresh() {
        try {
            accModel.setRowCount(0);
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            List<Object[]> rows = accountDAO.listAll();
            for (Object[] r : rows) {
                accModel.addRow(r);
                total = total.add((java.math.BigDecimal) r[5]);
            }
            summary.setText("   Customers: " + rows.size() + "   |   Total deposits: " + UIUtil.money(total));

            txModel.setRowCount(0);
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy HH:mm");
            for (Transaction t : txDAO.history(null, 300)) {
                txModel.addRow(new Object[]{t.getId(), fmt.format(t.getCreatedAt()), t.getAccountNo(),
                        t.getType(), UIUtil.money(t.getAmount()),
                        t.getOtherAccount() == null ? "-" : t.getOtherAccount()});
            }
        } catch (BankException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
