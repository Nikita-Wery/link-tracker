package backend.academy.linktracker.bot.dialog.trackdialog;

import backend.academy.linktracker.bot.dialog.DialogState;

public enum TrackingDialogStates implements DialogState {
    IDLE,
    WAITING_URL,
    WAITING_TAGS,
    DONE
}
