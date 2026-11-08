package bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private final long id, accountNo;
    private final String type;
    private final BigDecimal amount, balanceAfter;
    private final Long otherAccount;
    private final Timestamp createdAt;

    public Transaction(long id, long accountNo, String type, BigDecimal amount,
                       Long otherAccount, BigDecimal balanceAfter, Timestamp createdAt) {
        this.id = id; this.accountNo = accountNo; this.type = type; this.amount = amount;
        this.otherAccount = otherAccount; this.balanceAfter = balanceAfter; this.createdAt = createdAt;
    }
    public long getId() { return id; }
    public long getAccountNo() { return accountNo; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Long getOtherAccount() { return otherAccount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Timestamp getCreatedAt() { return createdAt; }
}
