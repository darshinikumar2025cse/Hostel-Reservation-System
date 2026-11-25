package hostel.model;

import java.time.LocalDate;

/** Payment status values: Paid, Pending, Partial. */
public record Payment(String id, String reservationId, String studentName, double amount,
                      String method, LocalDate date, String status) { }
