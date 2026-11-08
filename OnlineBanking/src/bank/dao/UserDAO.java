package bank.dao;

import bank.BankException;
import bank.db.DBConnection;
import bank.model.User;
import bank.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    /** Creates the default admin on first run. */
    public void ensureAdmin() throws BankException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,?,?,?,'ADMIN')")) {
                    ins.setString(1, "Administrator");
                    ins.setString(2, "admin@bank.com");
                    ins.setString(3, "");
                    ins.setString(4, PasswordUtil.hash("admin123"));
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Registers a customer and opens their account atomically. Returns the new account number. */
    public long register(String name, String email, String phone, String password, String accType)
            throws BankException {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE email=?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new BankException("This email is already registered.");
                    }
                }
                int userId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,?,?,?,'CUSTOMER')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, PasswordUtil.hash(password));
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); userId = k.getInt(1); }
                }
                long accNo;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO accounts(user_id,type) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, accType);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); accNo = k.getLong(1); }
                }
                c.commit();
                return accNo;
            } catch (BankException | SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Returns the User on success, or null if the credentials are wrong. */
    public User login(String email, String password) throws BankException {
        String sql = "SELECT id,name,email,phone,password_hash,role FROM users WHERE email=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password_hash"))) {
                    return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                            rs.getString("phone"), rs.getString("role"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public void changePassword(int userId, String oldPw, String newPw) throws BankException {
        try (Connection c = DBConnection.get()) {
            String stored;
            try (PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE id=?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new BankException("User not found.");
                    stored = rs.getString(1);
                }
            }
            if (!PasswordUtil.verify(oldPw, stored)) throw new BankException("Current password is incorrect.");
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
                ps.setString(1, PasswordUtil.hash(newPw));
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }
}
