package backend.academy.linktracker.bot.dialoge.trackdialog;

import backend.academy.linktracker.bot.dialoge.DialogState;

public enum TrackingDialogStates implements DialogState {
    IDLE,
    WAITING_URL,
    WAITING_TAGS,
    WAITING_FILTERS,
    DONE
}
