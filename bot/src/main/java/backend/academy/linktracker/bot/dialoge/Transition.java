package backend.academy.linktracker.bot.dialoge;

import backend.academy.linktracker.bot.dialoge.trackdialog.TrackingDialogStates;

public record Transition(
    DialogState from,
    DialogState to,
    StateAction action)
{}
