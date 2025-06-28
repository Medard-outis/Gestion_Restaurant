package org.example.gestion_restaurant.models;

import javafx.beans.property.*;

public class OrderItem {
    private final IntegerProperty id;
    private final IntegerProperty orderId;
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty totalPrice;
    private final StringProperty notes;

    public OrderItem() {
        this(0, 0, 0, "", 0, 0.0, "");
    }

    public OrderItem(int id, int orderId, int productId, String productName, 
                     int quantity, double unitPrice, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.orderId = new SimpleIntegerProperty(orderId);
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty();
        this.notes = new SimpleStringProperty(notes);
        
        // Calculer automatiquement le prix total
        updateTotalPrice();
        
        // Listeners pour recalculer le prix total
        this.quantity.addListener((obs, oldVal, newVal) -> updateTotalPrice());
        this.unitPrice.addListener((obs, oldVal, newVal) -> updateTotalPrice());
    }

    public OrderItem(int orderId, int productId, String productName, 
                     int quantity, double unitPrice, String notes) {
        this(0, orderId, productId, productName, quantity, unitPrice, notes);
    }

    private void updateTotalPrice() {
        totalPrice.set(quantity.get() * unitPrice.get());
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty orderIdProperty() { return orderId; }
    public IntegerProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public StringProperty notesProperty() { return notes; }

    // Getters
    public int getId() { return id.get(); }
    public int getOrderId() { return orderId.get(); }
    public int getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public String getNotes() { return notes.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setUnitPrice(double unitPrice) { this.unitPrice.set(unitPrice); }
    public void setNotes(String notes) { this.notes.set(notes); }

    @Override
    public String toString() {
        return String.format("OrderItem{productName='%s', quantity=%d, unitPrice=%.2f, total=%.2f}", 
                           getProductName(), getQuantity(), getUnitPrice(), getTotalPrice());
    }
}