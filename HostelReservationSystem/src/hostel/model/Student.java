package hostel.model;

/** Simple immutable data holder for a student. */
public record Student(String id, String name, String email, String phone,
                      String course, String gender, String address) { }
