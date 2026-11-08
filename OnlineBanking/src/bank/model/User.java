package bank.model;

public class User {
    private final int id;
    private final String name, email, phone, role;

    public User(int id, String name, String email, String phone, String role) {
        this.id = id; this.name = name; this.email = email; this.phone = phone; this.role = role;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
