package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Facture {
    private final IntegerProperty id;
    private final StringProperty clientName;
    private final IntegerProperty tableNumber;
    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty totalAmount;
    private final BooleanProperty isPaid;
    private final StringProperty status;
    private final List<FactureItem> items;

    public Facture() {
        this(0, "", 0, LocalDate.now(), 0.0, false, "EN_COURS");
    }

    public Facture(int id, String clientName, int tableNumber, LocalDate date, double totalAmount, boolean isPaid, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.clientName = new SimpleStringProperty(clientName);
        this.tableNumber = new SimpleIntegerProperty(tableNumber);
        this.date = new SimpleObjectProperty<>(date);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.isPaid = new SimpleBooleanProperty(isPaid);
        this.status = new SimpleStringProperty(status);
        this.items = new ArrayList<>();
    }

    public Facture(String clientName, int tableNumber, LocalDate date, double totalAmount, boolean isPaid, String status) {
        this(0, clientName, tableNumber, date, totalAmount, isPaid, status);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty clientNameProperty() { return clientName; }
    public IntegerProperty tableNumberProperty() { return tableNumber; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public BooleanProperty isPaidProperty() { return isPaid; }
    public StringProperty statusProperty() { return status; }

    // Getters
    public int getId() { return id.get(); }
    public String getClientName() { return clientName.get(); }
    public int getTableNumber() { return tableNumber.get(); }
    public LocalDate getDate() { return date.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public boolean isPaid() { return isPaid.get(); }
    public String getStatus() { return status.get(); }
    public List<FactureItem> getItems() { return items; }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setClientName(String clientName) { this.clientName.set(clientName); }
    public void setTableNumber(int tableNumber) { this.tableNumber.set(tableNumber); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }
    public void setIsPaid(boolean isPaid) { this.isPaid.set(isPaid); }
    public void setStatus(String status) { this.status.set(status); }

    public void setItems(List<FactureItem> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        calculateTotal();
    }

    public void addItem(FactureItem item) {
        items.add(item);
        calculateTotal();
    }

    public void removeItem(FactureItem item) {
        items.remove(item);
        calculateTotal();
    }

    private void calculateTotal() {
        double total = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        setTotalAmount(total);
    }
}