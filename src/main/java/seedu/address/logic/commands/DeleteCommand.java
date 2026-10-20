package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.FinanceQueries;
import seedu.address.model.finance.Ledger;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Deletes a student identified using the index shown in the displayed student list.
 * A student with lessons, homework, tasks or financial history requires confirmation, because deleting a student
 * also deletes all of those records. The latest deletion can be taken back once with {@code undo}.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer) [" + CONFIRM_KEYWORD + "]\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "Linked lessons, homework, tasks, and financial history require '" + CONFIRM_KEYWORD + "', "
            + "e.g. " + COMMAND_WORD + " 1 " + CONFIRM_KEYWORD;

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted student: %1$s";
    public static final String MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS =
            "Deleted student: %1$s, along with %2$d lesson(s).";
    public static final String MESSAGE_CONFIRMATION = "%1$s (phone: %2$s) has %3$d lesson(s). "
            + "These will all be deleted.\n%4$sType '" + COMMAND_WORD + " %5$d " + CONFIRM_KEYWORD + "' to proceed.";
    public static final String MESSAGE_UPCOMING_WARNING = "Warning: this student has %1$d upcoming lesson(s).\n";

    private final Index targetIndex;
    private final boolean isConfirmed;

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex} only if the student
     * has no lessons.
     */
    public DeleteCommand(Index targetIndex) {
        this(targetIndex, false);
    }

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex}.
     * A student who has lessons, homework, or financial history is only deleted if {@code isConfirmed} is true.
     */
    public DeleteCommand(Index targetIndex, boolean isConfirmed) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public Optional<String> undoLabel() {
        return Optional.of("student deletion");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.invalidPersonIndex(targetIndex, lastShownList.size()));
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        LinkedRecords records = LinkedRecords.of(model, personToDelete);

        if (records.isEmpty()) {
            model.deletePerson(personToDelete);
            return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
        }

        if (!isConfirmed) {
            return new CommandResult(getConfirmationPreview(model, personToDelete, records));
        }

        model.deletePerson(personToDelete);
        return new CommandResult(getSuccessMessage(personToDelete, records));
    }

    /**
     * Returns what deleting {@code student} would delete along with them, and how to confirm. A student with
     * homework, financial events or tasks also gets their counts, their balance and the advice to archive instead.
     */
    private String getConfirmationPreview(Model model, Person student, LinkedRecords records) {
        String preview = getConfirmationPrompt(student, records.lessons());
        if (!records.hasHistoryBeyondLessons()) {
            return preview;
        }
        return preview + "\n" + describeHistory(records) + "\nBalance: "
                + FinanceQueries.balanceLabel(Ledger.balance(model.getAddressBook(), student.getId()))
                + ". Use 'archive INDEX' to retain these obligations and all history instead.";
    }

    /**
     * Returns the line that counts the homework, financial events and tasks that would be deleted. Feedback is
     * mentioned only when there is some, because it is the part of the history that took thought to write.
     */
    private static String describeHistory(LinkedRecords records) {
        long upcoming = records.homework().stream().filter(Homework::isOutstanding)
                .filter(item -> !item.getDueDate().isBefore(LocalDate.now())).count();
        String homework = records.homework().size() + " homework item(s), including " + upcoming
                + " upcoming homework item(s)";
        String events = records.financialEvents() + " financial event(s)";
        String line = records.tasks() == 0
                ? "This also deletes " + homework + ", and " + events + "."
                : "This also deletes " + homework + ", " + events + ", and " + records.tasks() + " task(s).";
        long withFeedback = records.homework().stream().filter(item -> item.getFeedback().isPresent()).count();
        return withFeedback == 0
                ? line
                : line + " Recorded feedback on " + withFeedback + " homework item(s) will be deleted.";
    }

    /** Returns what was deleted along with {@code student}, listing every kind of record that was present. */
    private static String getSuccessMessage(Person student, LinkedRecords records) {
        if (!records.hasHistoryBeyondLessons()) {
            return String.format(MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS, Messages.format(student),
                    records.lessons().size());
        }
        List<String> parts = new ArrayList<>();
        parts.add(records.lessons().size() + " lesson(s)");
        parts.add(records.homework().size() + " homework item(s)");
        if (records.financialEvents() > 0 || records.tasks() > 0) {
            parts.add(records.financialEvents() + " financial event(s)");
        }
        if (records.tasks() > 0) {
            parts.add(records.tasks() + " task(s)");
        }
        return "Deleted student: " + Messages.format(student) + ", along with " + joinWithAnd(parts) + ".";
    }

    /** Joins two items with "and", and more with commas and a final "and". */
    private static String joinWithAnd(List<String> parts) {
        if (parts.size() == 2) {
            return parts.get(0) + " and " + parts.get(1);
        }
        return String.join(", ", parts.subList(0, parts.size() - 1)) + ", and " + parts.getLast();
    }

    /** The records that are deleted together with a student. */
    private record LinkedRecords(List<Lesson> lessons, List<Homework> homework, long financialEvents, long tasks) {
        static LinkedRecords of(Model model, Person student) {
            List<Lesson> lessons = model.getAddressBook().getLessonList().stream()
                    .filter(lesson -> lesson.isWith(student)).toList();
            List<Homework> homework = model.getAddressBook().getHomeworkList().stream()
                    .filter(item -> item.getStudentId().equals(student.getId())).toList();
            long financialEvents = model.getAddressBook().getLedgerEntries().stream()
                    .filter(entry -> entry.studentId().equals(student.getId())).count();
            long tasks = model.getAddressBook().getTaskList().stream()
                    .filter(task -> task.studentId().equals(student.getId())).count();
            return new LinkedRecords(lessons, homework, financialEvents, tasks);
        }

        boolean hasHistoryBeyondLessons() {
            return !homework.isEmpty() || financialEvents > 0 || tasks > 0;
        }

        boolean isEmpty() {
            return lessons.isEmpty() && !hasHistoryBeyondLessons();
        }
    }

    /**
     * Returns the message that asks the user to confirm deleting {@code student} and their {@code lessons}.
     * The message shows the name and phone number of the student, so that the user can check that the right
     * student is about to be deleted.
     */
    private String getConfirmationPrompt(Person student, List<Lesson> lessons) {
        LocalDate today = LocalDate.now();
        long upcomingCount = lessons.stream().filter(lesson -> lesson.isUpcoming(today)).count();
        String upcomingWarning = upcomingCount == 0
                ? ""
                : String.format(MESSAGE_UPCOMING_WARNING, upcomingCount);
        return String.format(MESSAGE_CONFIRMATION, student.getName(), student.getPhone(), lessons.size(),
                upcomingWarning, targetIndex.getOneBased());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex)
                && isConfirmed == otherDeleteCommand.isConfirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
