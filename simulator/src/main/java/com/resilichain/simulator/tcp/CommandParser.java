package com.resilichain.simulator.tcp;

import java.util.Locale;

public final class CommandParser {

    private static final String DEFAULT_SEVERITY = "HIGH";
    private static final int DEFAULT_DURATION_HOURS = 36;

    private CommandParser() {
    }

    public static Command parse(String line) {
        if (line == null || line.isBlank()) {
            return new Command.Unknown("");
        }

        String[] parts = line.trim().split("\\s+");
        String verb = parts[0].toUpperCase(Locale.ROOT);

        return switch (verb) {
            case "START" -> new Command.Start();
            case "STOP" -> new Command.Stop();
            case "STATUS" -> new Command.Status();
            case "HELP" -> new Command.Help();
            case "QUIT", "EXIT" -> new Command.Quit();
            case "DISRUPT" -> parseDisrupt(parts);
            default -> new Command.Unknown(line.trim());
        };
    }

    private static Command parseDisrupt(String[] parts) {
        String severity = parts.length > 1 ? parts[1].toUpperCase(Locale.ROOT) : DEFAULT_SEVERITY;
        int durationHours = DEFAULT_DURATION_HOURS;
        if (parts.length > 2) {
            try {
                durationHours = Integer.parseInt(parts[2]);
            } catch (NumberFormatException ignored) {
                durationHours = DEFAULT_DURATION_HOURS;
            }
        }
        return new Command.Disrupt(severity, durationHours);
    }
}
