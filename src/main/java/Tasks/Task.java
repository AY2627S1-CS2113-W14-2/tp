package Tasks;

/** Holds the description shared by all task types. */
public abstract class Task {
    /** Description entered by the user and displayed by each task type. */
    protected final String description;

    /** Creates the shared part of a task with its description. */
    public Task(String description) {
        this.description = description;
    }

    /** Returns the task description without status or date information. */
    public String getDescription() {
        return description;
    }
}
