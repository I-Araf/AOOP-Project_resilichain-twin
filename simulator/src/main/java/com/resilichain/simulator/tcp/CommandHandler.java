package com.resilichain.simulator.tcp;

@FunctionalInterface
public interface CommandHandler {
    String handle(Command command);
}
