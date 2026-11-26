
package com.example.beauty;

public class User {

    private final int id;
    private final String username;
    private final String phone;
    private final String email;

    public User(int id, String username,
                String phone, String email) {

        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
