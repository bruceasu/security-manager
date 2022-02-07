package me.asu.security.cmd;

public interface Command {
    String name();
    int execute(String... args);
    String description();
}
