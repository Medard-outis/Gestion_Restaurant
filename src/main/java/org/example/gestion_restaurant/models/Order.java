package org.example.gestion_restaurant.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private final IntegerProperty id;
    private final IntegerProperty tableNumber;
    private final StringProperty customerName;
    private final ObjectProperty<LocalDateTime> orderTime;
    private final DoubleProperty totalAmount;
    private final StringProperty status;
    private final BooleanProperty isPaid;
    private final ObservableList<OrderItem> items;

    public enum OrderStatus {
        EN_ATTENTE("En attente"),
        EN_PREPARATION("En préparation"),
        PRET("Prêt"),
        SERVI("Servi"),
        ANNULE("Annulé");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Order() {
        this(0, 0, "", LocalDateTime.now(), 0.0, OrderStatus.EN_ATTENTE.getDisplayName(), false);
    }

    public Order(int id, int tableNumber, String customerName, LocalDateTime orderTime, 
                 double totalAmount, String status, boolean isPaid) {
        this.id = new SimpleIntegerProperty(id);
        this.tableNumber = new SimpleIntegerProperty(tableNumber);
        this.customerName = new SimpleStringProperty(customerName);
        this.orderTime = new SimpleObjectProperty<>(orderTime);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.status = new SimpleStringProperty(status);
        this.isPaid = new SimpleBooleanProperty(isPaid);
        this.items = FXCollections.observableArrayList();
    }

    public Order(int tableNumber, String customerName, LocalDateTime orderTime, 
                 double totalAmount, String status, boolean isPaid) {
        this(0, tableNumber, customerName, orderTime, totalAmount, status, isPaid);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty tableNumberProperty() { return tableNumber; }
    public StringProperty customerNameProperty() { return customerName; }
    public ObjectProperty<LocalDateTime> orderTimeProperty() { return orderTime; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public StringProperty statusProperty() { return status; }
    public BooleanProperty isPaidProperty() { return isPaid; }

    // Getters
    public int getId() { return id.get(); }
    public int getTableNumber() { return tableNumber.get(); }
    public String getCustomerName() { return customerName.get(); }
    public LocalDateTime getOrderTime() { return orderTime.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public String getStatus() { return status.get(); }
    public boolean isPaid() { return isPaid.get(); }
    public ObservableList<OrderItem> getItems() { return items; }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setTableNumber(int tableNumber) { this.tableNumber.set(tableNumber); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime.set(orderTime); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }
    public void setStatus(String status) { this.status.set(status); }
    public void setIsPaid(boolean isPaid) { this.isPaid.set(isPaid); }

    // Méthodes pour gérer les items
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        calculateTotal();
    }

    public void clearItems() {
        items.clear();
        setTotalAmount(0.0);
    }

    private void calculateTotal() {
        double total = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        setTotalAmount(total);
    }

    // Méthodes utilitaires pour le statut
    public void markAsInPreparation() {
        setStatus(OrderStatus.EN_PREPARATION.getDisplayName());
    }

    public void markAsReady() {
        setStatus(OrderStatus.PRET.getDisplayName());
    }

    public void markAsServed() {
        setStatus(OrderStatus.SERVI.getDisplayName());
    }

    public void markAsCancelled() {
        setStatus(OrderStatus.ANNULE.getDisplayName());
    }

    public void markAsPaid() {
        setIsPaid(true);
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, table=%d, customer='%s', total=%.2f, status='%s'}", 
                           getId(), getTableNumber(), getCustomerName(), getTotalAmount(), getStatus());
    }
}