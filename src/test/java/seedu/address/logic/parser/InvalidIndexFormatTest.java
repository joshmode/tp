package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkFeedbackCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.commands.LessonPrepCommand;
import seedu.address.logic.commands.LessonRepeatCommand;
import seedu.address.logic.commands.LessonSeriesCancelCommand;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.PayListCommand;
import seedu.address.logic.commands.PayReverseCommand;
import seedu.address.logic.commands.TaskAddCommand;
import seedu.address.logic.commands.TaskDeleteCommand;
import seedu.address.logic.commands.TaskDoneCommand;
import seedu.address.logic.commands.TaskEditCommand;
import seedu.address.logic.commands.TaskListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Every command that takes an index reports an index that is not a positive whole number in the same way:
 * as an invalid command format followed by that command's usage string.
 */
public class InvalidIndexFormatTest {
    private final AddressBookParser parser = new AddressBookParser();

    @TestFactory
    public Stream<DynamicTest> parseCommand_indexNotPositiveWholeNumber_showsInvalidFormatAndUsage() {
        Map<String, String> usageByInput = new LinkedHashMap<>();
        usageByInput.put("archive 0", ArchiveCommand.MESSAGE_USAGE_ARCHIVE);
        usageByInput.put("student archive abc", ArchiveCommand.MESSAGE_USAGE_ARCHIVE);
        usageByInput.put("restore abc", ArchiveCommand.MESSAGE_USAGE_RESTORE);
        usageByInput.put("restore", ArchiveCommand.MESSAGE_USAGE_RESTORE);
        usageByInput.put("lesson prep 0 n/x", LessonPrepCommand.MESSAGE_USAGE);
        usageByInput.put("lesson series cancel abc confirm", LessonSeriesCancelCommand.MESSAGE_USAGE);
        usageByInput.put("lesson repeat st/0 d/2030-01-01 t/10:00 count/2", LessonRepeatCommand.MESSAGE_USAGE);
        usageByInput.put("lesson add st/0 d/2030-01-01 t/10:00", LessonAddCommand.MESSAGE_USAGE);
        usageByInput.put("lesson list st/abc", LessonListCommand.MESSAGE_USAGE);
        usageByInput.put("hw add st/0 desc/x due/2030-01-01", HomeworkAddCommand.MESSAGE_USAGE);
        usageByInput.put("hw list st/0", HomeworkListCommand.MESSAGE_USAGE);
        usageByInput.put("hw edit 0 desc/x", HomeworkEditCommand.MESSAGE_USAGE);
        usageByInput.put("hw status abc s/submitted", HomeworkStatusCommand.MESSAGE_USAGE);
        usageByInput.put("hw feedback 0 f/x", HomeworkFeedbackCommand.MESSAGE_USAGE);
        usageByInput.put("hw delete 0", HomeworkDeleteCommand.MESSAGE_USAGE);
        usageByInput.put("pay st/0 amt/5", PayCommand.MESSAGE_USAGE);
        usageByInput.put("pay list st/0", PayListCommand.MESSAGE_USAGE);
        usageByInput.put("pay reverse 0", PayReverseCommand.MESSAGE_USAGE);
        usageByInput.put("pay reverse", PayReverseCommand.MESSAGE_USAGE);
        usageByInput.put("balance st/0", BalanceCommand.MESSAGE_USAGE);
        usageByInput.put("charge l/0", ChargeCommand.MESSAGE_USAGE);
        usageByInput.put("task add st/0 desc/x", TaskAddCommand.MESSAGE_USAGE);
        usageByInput.put("task add lesson/abc desc/x", TaskAddCommand.MESSAGE_USAGE);
        usageByInput.put("task list st/0", TaskListCommand.MESSAGE_USAGE);
        usageByInput.put("task edit 0 desc/x", TaskEditCommand.MESSAGE_USAGE);
        usageByInput.put("task done 0", TaskDoneCommand.MESSAGE_USAGE);
        usageByInput.put("task delete abc", TaskDeleteCommand.MESSAGE_USAGE);

        return usageByInput.entrySet().stream().map(entry ->
                DynamicTest.dynamicTest(entry.getKey(), () -> assertInvalidFormat(entry.getKey(), entry.getValue())));
    }

    private void assertInvalidFormat(String input, String usage) {
        Executable parsing = () -> parser.parseCommand(input);
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage), parsing);
    }
}
