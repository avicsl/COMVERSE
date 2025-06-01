package Class;

public class CurrentUser {
    private static CurrentUser instance = new CurrentUser();
    private Student student;

    private CurrentUser() {}

    public static CurrentUser getInstance() {
        return instance;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
