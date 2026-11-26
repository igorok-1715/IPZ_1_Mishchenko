
package com.example.beauty;

public class User {

    private final String username;
    private final String phone;
    private final String email;
    private final String password;

    public User(String username, String phone,
                String email, String password) {

        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }
}
