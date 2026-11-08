package bank.dao;

import bank.BankException;
import bank.db.DBConnection;
import bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /** Inserts a ledger row using the caller's connection so it joins the caller's DB transaction. */
    static void insert(Connection c, long accNo, String type, BigDecimal amount,
                       Long other, BigDecimal balanceAfter) throws SQLException {
        String sql = "INSERT INTO transactions(account_no,type,amount,other_account,balance_after) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            if (other == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, other);
            ps.setBigDecimal(5, balanceAfter);
            ps.executeUpdate();
        }
    }

    /** Latest transactions for one account (pass accNo = null for all accounts, used by admin). */
    public List<Transaction> history(Long accNo, int limit) throws BankException {
        String sql = "SELECT id,account_no,type,amount,other_account,balance_after,created_at FROM transactions "
                + (accNo != null ? "WHERE account_no=? " : "") + "ORDER BY id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (accNo != null) ps.setLong(i++, accNo);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long other = rs.getLong("other_account");
                    Long otherObj = rs.wasNull() ? null : other;
                    list.add(new Transaction(rs.getLong("id"), rs.getLong("account_no"), rs.getString("type"),
                            rs.getBigDecimal("amount"), otherObj, rs.getBigDecimal("balance_after"),
                            rs.getTimestamp("created_at")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }
}
