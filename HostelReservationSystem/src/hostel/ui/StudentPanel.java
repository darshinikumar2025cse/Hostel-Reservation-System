package hostel.ui;

import hostel.model.Student;
import hostel.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Student Management: add, update, delete, view and search students. */
public class StudentPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();

    private final JTextField idField = Theme.readOnly();
    private final JTextField nameField = Theme.field();
    private final JTextField emailField = Theme.field();
    private final JTextField phoneField = Theme.field();
    private final JTextField courseField = Theme.field();
    private final JComboBox<String> genderBox = Theme.combo(new String[]{"Male", "Female", "Other"});
    private final JTextField addressField = Theme.field();
    private final JTextField searchField = Theme.field();

    private final DefaultTableModel model = Theme.model("ID", "Name", "Email", "Phone", "Course", "Gender", "Address");
    private final JTable table = new JTable(model);
    private String selectedId = null;

    public StudentPanel() {
        Theme.initPage(this);
        add(Theme.pageTitle("Student Management", "Add, update, delete and search hostel students"), BorderLayout.NORTH);

        // ---- left: form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, 0, "Student ID", idField);
        Theme.addRow(form, 1, "Full Name *", nameField);
        Theme.addRow(form, 2, "Email *", emailField);
        Theme.addRow(form, 3, "Phone (10 digits) *", phoneField);
        Theme.addRow(form, 4, "Course *", courseField);
        Theme.addRow(form, 5, "Gender", genderBox);
        Theme.addRow(form, 6, "Address *", addressField);

        JButton addBtn = Theme.primary("Add Student");
        JButton updateBtn = Theme.success("Update");
        JButton deleteBtn = Theme.danger("Delete");
        JButton clearBtn = Theme.neutral("Clear");
        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        clearBtn.addActionListener(e -> clearForm());

        JScrollPane side = Theme.wrap(
                Theme.formCard("Student Details", form, Theme.buttonGrid(2, addBtn, updateBtn, deleteBtn, clearBtn)),
                BorderLayout.NORTH);
        side.setPreferredSize(new Dimension(350, 10));

        // ---- right: search + table
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        JButton searchBtn = Theme.primary("Search");
        searchBtn.addActionListener(e -> reload());
        searchField.setToolTipText("Search by ID, name, course or phone");
        Theme.onChange(searchField, this::reload);
        searchField.addActionListener(e -> reload());

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.add(Theme.label("Search Student:", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(searchBtn, BorderLayout.EAST);

        Theme.Card tableCard = new Theme.Card(new BorderLayout(0, 12));
        tableCard.add(bar, BorderLayout.NORTH);
        tableCard.add(Theme.tableScroll(table), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(side, BorderLayout.WEST);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        clearForm();
        reload();
    }

    // ------------------------------------------------------------ actions
    private void addStudent() {
        String err = validateInput();
        if (err != null) { Theme.error(this, err); return; }
        Student s = buildStudent(ds.nextStudentId());
        ds.addStudent(s);
        reload();
        clearForm();
        Theme.success(this, "Student \"" + s.name() + "\" added successfully.");
    }

    private void updateStudent() {
        if (selectedId == null) { Theme.error(this, "Please select a student from the table to update."); return; }
        String err = validateInput();
        if (err != null) { Theme.error(this, err); return; }
        ds.updateStudent(buildStudent(selectedId));
        reload();
        clearForm();
        Theme.success(this, "Student details updated successfully.");
    }

    private void deleteStudent() {
        if (selectedId == null) { Theme.error(this, "Please select a student from the table to delete."); return; }
        if (!Theme.confirm(this, "Delete student " + selectedId + " (" + nameField.getText().trim() + ")?")) return;
        if (!ds.deleteStudent(selectedId)) {
            Theme.error(this, "This student has an active reservation and cannot be deleted.");
            return;
        }
        reload();
        clearForm();
        Theme.success(this, "Student deleted successfully.");
    }

    private Student buildStudent(String id) {
        return new Student(id, nameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(),
                courseField.getText().trim(), (String) genderBox.getSelectedItem(), addressField.getText().trim());
    }

    private String validateInput() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        if (name.isEmpty()) return "Full name is required.";
        if (!name.matches("[A-Za-z .'-]{2,}")) return "Name must contain letters only (minimum 2 characters).";
        if (email.isEmpty()) return "Email is required.";
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) return "Please enter a valid email address.";
        if (!phone.matches("\\d{10}")) return "Phone number must contain exactly 10 digits.";
        if (courseField.getText().trim().isEmpty()) return "Course is required.";
        if (addressField.getText().trim().isEmpty()) return "Address is required.";
        return null;
    }

    // --------------------------------------------------------------- state
    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (String) model.getValueAt(row, 0);
        idField.setText(selectedId);
        nameField.setText((String) model.getValueAt(row, 1));
        emailField.setText((String) model.getValueAt(row, 2));
        phoneField.setText((String) model.getValueAt(row, 3));
        courseField.setText((String) model.getValueAt(row, 4));
        genderBox.setSelectedItem(model.getValueAt(row, 5));
        addressField.setText((String) model.getValueAt(row, 6));
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        idField.setText(ds.nextStudentId() + " (auto)");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        courseField.setText("");
        genderBox.setSelectedIndex(0);
        addressField.setText("");
    }

    private void reload() {
        model.setRowCount(0);
        for (Student s : ds.searchStudents(searchField.getText())) {
            model.addRow(new Object[]{s.id(), s.name(), s.email(), s.phone(), s.course(), s.gender(), s.address()});
        }
    }

    @Override public void refresh() {
        reload();
        if (selectedId == null) idField.setText(ds.nextStudentId() + " (auto)");
    }
}
