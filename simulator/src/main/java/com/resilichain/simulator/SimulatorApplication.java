package com.resilichain.simulator;

import com.resilichain.simulator.event.DisruptionEvent;
import com.resilichain.simulator.kafka.EventProducer;
import com.resilichain.simulator.scenario.ChattogramScenario;
import com.resilichain.simulator.tcp.Command;
import com.resilichain.simulator.tcp.ControlServer;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standalone event source for the frozen Port Chattogram scenario: on a fixed interval it
 * advances one of the 12 seeded shipments through a legal lifecycle transition and publishes the
 * change to Kafka topic shipment-events; a TCP control channel lets a presenter trigger a
 * disruption-events message for the port on demand. Nothing here touches Postgres directly —
 * that happens in the Kafka consumer built in the next stage.
 */
public final class SimulatorApplication {

    public static void main(String[] args) throws InterruptedException {
        String bootstrapServers = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        int tcpPort = Integer.parseInt(env("SIMULATOR_TCP_PORT", "9099"));
        long tickIntervalSeconds = Long.parseLong(env("TICK_INTERVAL_SECONDS", "5"));

        EventProducer eventProducer = new EventProducer(bootstrapServers);
        ChattogramScenario scenario = new ChattogramScenario(new Random());
        AtomicBoolean running = new AtomicBoolean(true);

        ControlServer controlServer = new ControlServer(tcpPort, command -> handle(command, scenario, eventProducer, running));
        Thread tcpThread = new Thread(controlServer, "tcp-control-server");
        tcpThread.setDaemon(true);
        tcpThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(eventProducer::close));

        System.out.println("[Simulator] Chattogram scenario ready. bootstrapServers=" + bootstrapServers
                + " tcpControlPort=" + tcpPort + " tickIntervalSeconds=" + tickIntervalSeconds);
        System.out.println("[Simulator] Connect with: nc localhost " + tcpPort + "  (or telnet)");

        while (true) {
            if (running.get()) {
                scenario.tick().ifPresent(eventProducer::publishShipmentEvent);
            }
            TimeUnit.SECONDS.sleep(tickIntervalSeconds);
        }
    }

    private static String handle(Command command, ChattogramScenario scenario, EventProducer eventProducer, AtomicBoolean running) {
        return switch (command) {
            case Command.Start ignored -> {
                running.set(true);
                yield "Simulation running.";
            }
            case Command.Stop ignored -> {
                running.set(false);
                yield "Simulation paused.";
            }
            case Command.Status ignored -> scenario.statusSummary();
            case Command.Disrupt disrupt -> {
                DisruptionEvent event = scenario.disruptPort(disrupt.severity(), disrupt.durationHours());
                eventProducer.publishDisruptionEvent(event);
                yield "Published disruption: " + event;
            }
            case Command.Help ignored -> "Commands: START, STOP, STATUS, DISRUPT [severity] [hours], HELP, QUIT";
            case Command.Quit ignored -> "Bye.";
            case Command.Unknown unknown -> "Unknown command: '" + unknown.raw() + "'. Type HELP.";
        };
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
