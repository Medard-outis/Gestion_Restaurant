package org.example.gestion_restaurant.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty password;
    private final BooleanProperty isAdmin;
    private final ObjectProperty<LocalDate> createdAt;

    public User() {
        this(0, "", "", false, LocalDate.now());
    }

    public User(int id, String username, String password, boolean isAdmin, LocalDate createdAt) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.isAdmin = new SimpleBooleanProperty(isAdmin);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    public User(String username, String password, boolean isAdmin, LocalDate createdAt) {
        this(0, username, password, isAdmin, createdAt);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public BooleanProperty isAdminProperty() { return isAdmin; }
    public ObjectProperty<LocalDate> createdAtProperty() { return createdAt; }

    // Getters
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public boolean isAdmin() { return isAdmin.get(); }
    public LocalDate getCreatedAt() { return createdAt.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin.set(isAdmin); }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt.set(createdAt); }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', isAdmin=%s}", 
                           getId(), getUsername(), isAdmin());
    }
}