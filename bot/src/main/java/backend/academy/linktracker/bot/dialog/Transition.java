package backend.academy.linktracker.bot.dialog;

public record Transition(
    DialogState from,
    DialogState to,
    StateAction action)
{}
