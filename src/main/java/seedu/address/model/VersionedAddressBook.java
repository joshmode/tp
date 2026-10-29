package seedu.address.model;

import java.util.List;

import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * An extension to AddressBook, with additional undo capabilities.
 */
public class VersionedAddressBook extends AddressBook {
    private DestructiveActionType lastDestructiveAction = DestructiveActionType.NONE;
    private ReadOnlyAddressBook lastState;

    public VersionedAddressBook() {
        super();
    }

    public VersionedAddressBook(ReadOnlyAddressBook toBeCopied) {
        super(toBeCopied);
    }

    /**
     * Runs a destructive change and publishes an undo point only after it succeeds. A rejected deletion must
     * leave the previous undo point intact rather than replacing it with a state captured mid-operation.
     */
    private void performDestructiveAction(Runnable action, DestructiveActionType actionType) {
        ReadOnlyAddressBook stateBeforeAction = new AddressBook(this);
        action.run();
        lastState = stateBeforeAction;
        lastDestructiveAction = actionType;
    }

    private void resetDestructiveAction() {
        lastDestructiveAction = DestructiveActionType.NONE;
        lastState = null;
    }

    /**
     * Undoes the last destructive action performed on this AddressBook,
     * provided no attempts to modify it have been made.
     */
    public DestructiveActionType undoDestructiveAction() {
        if (lastDestructiveAction == DestructiveActionType.NONE) {
            return lastDestructiveAction;
        }
        DestructiveActionType type = lastDestructiveAction;
        // also resets destructive action
        this.resetData(this.lastState);
        return type;
    }

    // Overrides

    @Override
    public void setPersons(List<Person> persons) {
        super.setPersons(persons);
        resetDestructiveAction();
    }

    @Override
    public void setLessons(List<Lesson> lessons) {
        super.setLessons(lessons);
        resetDestructiveAction();
    }

    @Override
    public void setTasks(List<Task> tasks) {
        super.setTasks(tasks);
        resetDestructiveAction();
    }

    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        super.resetData(newData);
        this.resetDestructiveAction();
    }

    @Override
    public void clearData() {
        // Don't clear if already empty
        if (!isEmpty()) {
            performDestructiveAction(super::clearData, DestructiveActionType.CLEAR);
        }
    }

    @Override
    public void addPerson(Person p) {
        super.addPerson(p);
        resetDestructiveAction();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        super.setPerson(target, editedPerson);
        resetDestructiveAction();
    }

    @Override
    public void removePerson(Person key) {
        performDestructiveAction(() -> super.removePerson(key), DestructiveActionType.DELETE_STUDENT);
    }

    @Override
    public void addLesson(Lesson lesson) {
        super.addLesson(lesson);
        resetDestructiveAction();
    }

    @Override
    public void setLesson(Lesson target, Lesson editedLesson) {
        super.setLesson(target, editedLesson);
        resetDestructiveAction();
    }

    @Override
    public void removeLesson(Lesson key) {
        performDestructiveAction(() -> super.removeLesson(key), DestructiveActionType.DELETE_LESSON);
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        resetDestructiveAction();
    }

    @Override
    public void setTask(Task target, Task editedTask) {
        super.setTask(target, editedTask);
        resetDestructiveAction();
    }

    @Override
    public void removeTask(Task key) {
        performDestructiveAction(() -> super.removeTask(key), DestructiveActionType.DELETE_TASK);
    }

    @Override
    public void sortPersons(StudentOrder order) {
        super.sortPersons(order);
        resetDestructiveAction();
    }

    @Override
    public void addLedgerEntry(LedgerEntry entry) {
        super.addLedgerEntry(entry);
        resetDestructiveAction();
    }

    @Override
    public void setLedgerEntries(List<LedgerEntry> entries) {
        super.setLedgerEntries(entries);
        resetDestructiveAction();
    }

    @Override
    public void setHomework(Homework target, Homework replacement) {
        super.setHomework(target, replacement);
        resetDestructiveAction();
    }

    @Override
    public void addHomework(Homework item) {
        super.addHomework(item);
        resetDestructiveAction();
    }

    @Override
    public void removeHomework(Homework target) {
        performDestructiveAction(() -> super.removeHomework(target), DestructiveActionType.DELETE_HW);
    }

    @Override
    public void setTravelBuffer(int minutes) {
        super.setTravelBuffer(minutes);
        resetDestructiveAction();
    }
}
