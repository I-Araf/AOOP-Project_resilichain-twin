package com.resilichain.simulator.tcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserTest {

    @Test
    void parsesKnownVerbsCaseInsensitively() {
        assertThat(CommandParser.parse("start")).isInstanceOf(Command.Start.class);
        assertThat(CommandParser.parse("STOP")).isInstanceOf(Command.Stop.class);
        assertThat(CommandParser.parse("Status")).isInstanceOf(Command.Status.class);
        assertThat(CommandParser.parse("help")).isInstanceOf(Command.Help.class);
        assertThat(CommandParser.parse("quit")).isInstanceOf(Command.Quit.class);
        assertThat(CommandParser.parse("exit")).isInstanceOf(Command.Quit.class);
    }

    @Test
    void disruptWithoutArgsUsesDefaults() {
        Command command = CommandParser.parse("DISRUPT");

        assertThat(command).isInstanceOf(Command.Disrupt.class);
        Command.Disrupt disrupt = (Command.Disrupt) command;
        assertThat(disrupt.severity()).isEqualTo("HIGH");
        assertThat(disrupt.durationHours()).isEqualTo(36);
    }

    @Test
    void disruptWithArgsParsesSeverityAndDuration() {
        Command command = CommandParser.parse("disrupt critical 12");

        Command.Disrupt disrupt = (Command.Disrupt) command;
        assertThat(disrupt.severity()).isEqualTo("CRITICAL");
        assertThat(disrupt.durationHours()).isEqualTo(12);
    }

    @Test
    void disruptWithNonNumericDurationFallsBackToDefault() {
        Command command = CommandParser.parse("disrupt high notanumber");

        Command.Disrupt disrupt = (Command.Disrupt) command;
        assertThat(disrupt.durationHours()).isEqualTo(36);
    }

    @Test
    void blankOrNullInputIsUnknown() {
        assertThat(CommandParser.parse("")).isInstanceOf(Command.Unknown.class);
        assertThat(CommandParser.parse("   ")).isInstanceOf(Command.Unknown.class);
        assertThat(CommandParser.parse(null)).isInstanceOf(Command.Unknown.class);
    }

    @Test
    void unrecognizedVerbIsUnknownAndPreservesRawInput() {
        Command command = CommandParser.parse("  frobnicate now  ");

        assertThat(command).isInstanceOf(Command.Unknown.class);
        assertThat(((Command.Unknown) command).raw()).isEqualTo("frobnicate now");
    }
}
