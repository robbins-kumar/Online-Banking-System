package bank.dao;

import bank.BankException;
import bank.db.DBConnection;
import bank.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public Account findByUser(int userId) throws BankException {
        String sql = "SELECT account_no,user_id,type,balance,status FROM accounts WHERE user_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new BankException("No account found for this user.");
                return map(rs);
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public void deposit(long accNo, BigDecimal amount) throws BankException {
        checkAmount(amount);
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                Account a = lock(c, accNo);
                requireActive(a);
                BigDecimal nb = a.getBalance().add(amount);
                setBalance(c, accNo, nb);
                TransactionDAO.insert(c, accNo, "DEPOSIT", amount, null, nb);
                c.commit();
            } catch (BankException | SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public void withdraw(long accNo, BigDecimal amount) throws BankException {
        checkAmount(amount);
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                Account a = lock(c, accNo);
                requireActive(a);
                if (a.getBalance().compareTo(amount) < 0) throw new BankException("Insufficient balance.");
                BigDecimal nb = a.getBalance().subtract(amount);
                setBalance(c, accNo, nb);
                TransactionDAO.insert(c, accNo, "WITHDRAW", amount, null, nb);
                c.commit();
            } catch (BankException | SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Debit + credit in ONE database transaction: either both happen or neither does. */
    public void transfer(long from, long to, BigDecimal amount) throws BankException {
        checkAmount(amount);
        if (from == to) throw new BankException("Cannot transfer to the same account.");
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                // Lock rows in a consistent order to avoid deadlocks between opposite transfers.
                Account first = lock(c, Math.min(from, to));
                Account second = lock(c, Math.max(from, to));
                Account src = first.getAccountNo() == from ? first : second;
                Account dst = first.getAccountNo() == to ? first : second;

                requireActive(src);
                if (!dst.isActive()) throw new BankException("Destination account is frozen.");
                if (src.getBalance().compareTo(amount) < 0) throw new BankException("Insufficient balance.");

                BigDecimal srcBal = src.getBalance().subtract(amount);
                BigDecimal dstBal = dst.getBalance().add(amount);
                setBalance(c, from, srcBal);
                setBalance(c, to, dstBal);
                TransactionDAO.insert(c, from, "TRANSFER_OUT", amount, to, srcBal);
                TransactionDAO.insert(c, to, "TRANSFER_IN", amount, from, dstBal);
                c.commit();
            } catch (BankException | SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public void setStatus(long accNo, String status) throws BankException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE accounts SET status=? WHERE account_no=?")) {
            ps.setString(1, status);
            ps.setLong(2, accNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Admin view: rows of {accountNo, name, email, phone, type, balance, status}. */
    public List<Object[]> listAll() throws BankException {
        String sql = "SELECT a.account_no,u.name,u.email,u.phone,a.type,a.balance,a.status "
                + "FROM accounts a JOIN users u ON u.id=a.user_id ORDER BY a.account_no";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getBigDecimal(6), rs.getString(7)});
            }
            return rows;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    // ---------- helpers ----------

    private Account lock(Connection c, long accNo) throws SQLException, BankException {
        String sql = "SELECT account_no,user_id,type,balance,status FROM accounts WHERE account_no=? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new BankException("Account " + accNo + " not found.");
                return map(rs);
            }
        }
    }

    private void setBalance(Connection c, long accNo, BigDecimal bal) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance=? WHERE account_no=?")) {
            ps.setBigDecimal(1, bal);
            ps.setLong(2, accNo);
            ps.executeUpdate();
        }
    }

    private void checkAmount(BigDecimal amount) throws BankException {
        if (amount == null || amount.signum() <= 0) throw new BankException("Amount must be greater than zero.");
    }

    private void requireActive(Account a) throws BankException {
        if (!a.isActive()) throw new BankException("This account is frozen. Please contact the bank.");
    }

    private Account map(ResultSet rs) throws SQLException {
        return new Account(rs.getLong("account_no"), rs.getInt("user_id"), rs.getString("type"),
                rs.getBigDecimal("balance"), rs.getString("status"));
    }
}
