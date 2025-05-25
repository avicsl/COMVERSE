package Class;

import javafx.beans.property.SimpleStringProperty;

public class Student {

    private SimpleStringProperty studentNumber;
    private SimpleStringProperty email;
    private SimpleStringProperty password;
    private SimpleStringProperty firstName;
    private SimpleStringProperty lastName;
    private SimpleStringProperty department;
    private SimpleStringProperty course;

    // No-arg constructor (optional)
    public Student() {
        this.studentNumber = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.firstName = new SimpleStringProperty("");
        this.lastName = new SimpleStringProperty("");
        this.department = new SimpleStringProperty("");
        this.course = new SimpleStringProperty("");
    }

    // Full constructor
    public Student(String studentNumber, String email, String password, String firstName,
                   String lastName, String department, String course) {
        this.studentNumber = new SimpleStringProperty(studentNumber);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.department = new SimpleStringProperty(department);
        this.course = new SimpleStringProperty(course);
    }

    // Getters
    public String getStudentNumber() {
        return studentNumber.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getFirstName() {
        return firstName.get();
    }

    public String getLastName() {
        return lastName.get();
    }

    public String getDepartment() {
        return department.get();
    }

    public String getCourse() {
        return course.get();
    }

    // Setters
    public void setStudentNumber(String value) {
        studentNumber.set(value);
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public void setPassword(String value) {
        password.set(value);
    }

    public void setFirstName(String value) {
        firstName.set(value);
    }

    public void setLastName(String value) {
        lastName.set(value);
    }

    public void setDepartment(String value) {
        department.set(value);
    }

    public void setCourse(String value) {
        course.set(value);
    }

    // Property getters (for TableView bindings)
    public SimpleStringProperty studentNumberProperty() {
        return studentNumber;
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public SimpleStringProperty firstNameProperty() {
        return firstName;
    }

    public SimpleStringProperty lastNameProperty() {
        return lastName;
    }

    public SimpleStringProperty departmentProperty() {
        return department;
    }

    public SimpleStringProperty courseProperty() {
        return course;
    }
}
