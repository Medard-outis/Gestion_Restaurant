package org.example.gestion_restaurant.models;

import javafx.beans.property.*;

public class Table {
    private final IntegerProperty id;
    private final IntegerProperty number;
    private final IntegerProperty capacity;
    private final StringProperty status;
    private final BooleanProperty isOccupied;

    public enum TableStatus {
        LIBRE("Libre"),
        OCCUPEE("Occupée"),
        RESERVEE("Réservée"),
        NETTOYAGE("En nettoyage");

        private final String displayName;

        TableStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Table() {
        this(0, 0, 4, TableStatus.LIBRE.getDisplayName());
    }

    public Table(int id, int number, int capacity, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.number = new SimpleIntegerProperty(number);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.status = new SimpleStringProperty(status);
        this.isOccupied = new SimpleBooleanProperty();
        
        // Mettre à jour isOccupied basé sur le statut
        updateOccupiedStatus();
        this.status.addListener((obs, oldVal, newVal) -> updateOccupiedStatus());
    }

    public Table(int number, int capacity, String status) {
        this(0, number, capacity, status);
    }

    private void updateOccupiedStatus() {
        isOccupied.set(!TableStatus.LIBRE.getDisplayName().equals(status.get()));
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty numberProperty() { return number; }
    public IntegerProperty capacityProperty() { return capacity; }
    public StringProperty statusProperty() { return status; }
    public BooleanProperty isOccupiedProperty() { return isOccupied; }

    // Getters
    public int getId() { return id.get(); }
    public int getNumber() { return number.get(); }
    public int getCapacity() { return capacity.get(); }
    public String getStatus() { return status.get(); }
    public boolean isOccupied() { return isOccupied.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setNumber(int number) { this.number.set(number); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }
    public void setStatus(String status) { this.status.set(status); }

    // Méthodes utilitaires
    public void occupy() {
        setStatus(TableStatus.OCCUPEE.getDisplayName());
    }

    public void free() {
        setStatus(TableStatus.LIBRE.getDisplayName());
    }

    public void reserve() {
        setStatus(TableStatus.RESERVEE.getDisplayName());
    }

    public void startCleaning() {
        setStatus(TableStatus.NETTOYAGE.getDisplayName());
    }

    @Override
    public String toString() {
        return String.format("Table %d (%d places) - %s", getNumber(), getCapacity(), getStatus());
    }
}