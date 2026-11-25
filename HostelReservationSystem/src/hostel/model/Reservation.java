package hostel.model;

import java.time.LocalDate;

/** Reservation status values: Confirmed, Checked-In, Completed, Cancelled. */
public record Reservation(String id, String studentId, String studentName, String roomNo,
                          LocalDate checkIn, LocalDate checkOut, double total, String status) { }
