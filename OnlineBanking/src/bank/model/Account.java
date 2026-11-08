package bank.model;

import java.math.BigDecimal;

public class Account {
    private final long accountNo;
    private final int userId;
    private final String type, status;
    private final BigDecimal balance;

    public Account(long accountNo, int userId, String type, BigDecimal balance, String status) {
        this.accountNo = accountNo; this.userId = userId; this.type = type;
        this.balance = balance; this.status = status;
    }
    public long getAccountNo() { return accountNo; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public String getStatus() { return status; }
    public boolean isActive() { return "ACTIVE".equals(status); }
}
