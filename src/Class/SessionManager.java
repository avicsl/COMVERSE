package Class;

public class SessionManager {
    private static Student currentStudent;
 
    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }
 
    public static Student getCurrentStudent() {
        return currentStudent;
    }
 
    public static void clearSession() {
        currentStudent = null;
    }
}
 
 