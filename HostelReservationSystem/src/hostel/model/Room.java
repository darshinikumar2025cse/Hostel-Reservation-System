package hostel.model;

/** Room status values used by the UI: Available, Occupied, Maintenance. */
public record Room(String roomNo, String type, int capacity, double price, String status) { }
