package bank;

/** Checked exception carrying a user-friendly message. */
public class BankException extends Exception {
    public BankException(String msg) { super(msg); }
    public BankException(String msg, Throwable cause) { super(msg, cause); }
}
