package org.example.gestion_restaurant.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    private final StringProperty category;
    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty totalValue;

    public Product() {
        this(0, "", 0.0, 0, "", LocalDate.now());
    }

    public Product(int id, String name, double price, int quantity, String category, LocalDate date) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(date);
        this.totalValue = new SimpleDoubleProperty();
        
        // Calculer automatiquement la valeur totale
        updateTotalValue();
        
        // Listeners pour recalculer la valeur totale
        this.price.addListener((obs, oldVal, newVal) -> updateTotalValue());
        this.quantity.addListener((obs, oldVal, newVal) -> updateTotalValue());
    }

    public Product(String name, double price, int quantity, String category, LocalDate date) {
        this(0, name, price, quantity, category, date);
    }

    private void updateTotalValue() {
        totalValue.set(price.get() * quantity.get());
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty categoryProperty() { return category; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public DoubleProperty totalValueProperty() { return totalValue; }

    // Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getCategory() { return category.get(); }
    public LocalDate getDate() { return date.get(); }
    public double getTotalValue() { return totalValue.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setCategory(String category) { this.category.set(category); }
    public void setDate(LocalDate date) { this.date.set(date); }

    // Méthodes utilitaires
    public boolean isLowStock() {
        return quantity.get() > 0 && quantity.get() < 10;
    }

    public boolean isOutOfStock() {
        return quantity.get() == 0;
    }

    public void addStock(int amount) {
        if (amount > 0) {
            setQuantity(getQuantity() + amount);
        }
    }

    public boolean removeStock(int amount) {
        if (amount > 0 && getQuantity() >= amount) {
            setQuantity(getQuantity() - amount);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%.2f, quantity=%d, category='%s'}", 
                           getId(), getName(), getPrice(), getQuantity(), getCategory());
    }
}