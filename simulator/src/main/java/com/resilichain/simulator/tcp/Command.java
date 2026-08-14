package com.resilichain.simulator.tcp;

public sealed interface Command {
    record Start() implements Command {
    }

    record Stop() implements Command {
    }

    record Status() implements Command {
    }

    record Disrupt(String severity, int durationHours) implements Command {
    }

    record Help() implements Command {
    }

    record Quit() implements Command {
    }

    record Unknown(String raw) implements Command {
    }
}
