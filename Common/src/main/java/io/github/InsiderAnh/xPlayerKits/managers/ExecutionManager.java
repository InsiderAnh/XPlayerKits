package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.executions.enums.CommandType;
import io.github.InsiderAnh.xPlayerKits.executions.enums.MessageType;
import io.github.InsiderAnh.xPlayerKits.executions.enums.SoundType;
import io.github.InsiderAnh.xPlayerKits.executions.executions.*;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionManager {

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^(?:message:|\\[message])\\s*(.*)$");
    private static final Pattern CENTER_MESSAGE_PATTERN = Pattern.compile("^(?:center_message:|\\[center_message])\\s*(.*)$");
    private static final Pattern MINI_MESSAGE_PATTERN = Pattern.compile("^(?:mini_message:|\\[mini_message])\\s*(.*)$");
    private static final Pattern BROADCAST_PATTERN = Pattern.compile("^(?:broadcast:|\\[broadcast])\\s*(.*)$");
    private static final Pattern SOUND_PATTERN = Pattern.compile("^(?:sound:|\\[sound])\\s*(.*)$");
    private static final Pattern PLAYSOUND_RESOURCE_PATTERN = Pattern.compile("^(?:playsound_resource_pack:|\\[playsound_resource_pack])\\s*(.*)$");
    private static final Pattern PLAYER_COMMAND_PATTERN = Pattern.compile("^(?:command:|player_command:|\\[player]|\\[player_command])\\s*(.*)$");
    private static final Pattern CONSOLE_COMMAND_PATTERN = Pattern.compile("^(?:console_command:|console:|\\[console_command]|\\[console])\\s*(.*)$");
    private static final Pattern TITLES_PATTERN = Pattern.compile("^(?:titles:|\\[titles])\\s*(.*)$");
    private static final Pattern WAIT_TICKS_PATTERN = Pattern.compile("^(?:wait_ticks:|\\[wait_ticks])\\s*(.*)$");

    public Execution getExecution(String action) {
        if (action == null || action.trim().isEmpty()) {
            return null;
        }

        Matcher matcher;

        matcher = MESSAGE_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteMessage(action, MessageType.NORMAL, matcher.group(1));
        }

        matcher = CENTER_MESSAGE_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteMessage(action, MessageType.CENTERED, matcher.group(1));
        }

        matcher = MINI_MESSAGE_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteMessage(action, MessageType.MINI_MESSAGE, matcher.group(1));
        }

        matcher = BROADCAST_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteMessage(action, MessageType.BROADCAST, matcher.group(1));
        }

        matcher = SOUND_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteSound(action, SoundType.NORMAL_SOUND, matcher.group(1));
        }

        matcher = PLAYSOUND_RESOURCE_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteSound(action, SoundType.RESOURCE_PACK_SOUND, matcher.group(1));
        }

        matcher = PLAYER_COMMAND_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteCommand(action, CommandType.PLAYER_COMMAND, matcher.group(1));
        }

        matcher = CONSOLE_COMMAND_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteCommand(action, CommandType.CONSOLE_COMMAND, matcher.group(1));
        }

        matcher = TITLES_PATTERN.matcher(action);
        if (matcher.matches()) {
            return new ExecuteTitles(action, matcher.group(1));
        }

        matcher = WAIT_TICKS_PATTERN.matcher(action);
        if (matcher.matches()) {
            try {
                return new ExecuteWaitTicks(action, Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public void execute(Player player, List<Execution> listToExecute, Placeholder... placeholders) {
        LinkedList<Execution> executions = new LinkedList<>(listToExecute);
        for (Execution execution : new LinkedList<>(executions)) {
            executions.remove(execution);
            if (execution instanceof ExecuteWaitTicks) {
                ExecuteWaitTicks waitTicks = (ExecuteWaitTicks) execution;
                waitTicks.executeActions(executions, player, placeholders);
            } else {
                execution.execute(player, placeholders);
            }
        }
    }

}