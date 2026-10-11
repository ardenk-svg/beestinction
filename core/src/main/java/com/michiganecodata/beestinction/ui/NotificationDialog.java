package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.michiganecodata.beestinction.sim.Notification;

/**
 * OVERVIEW:      Placeholder popup for a Notification that pauses the game (CLAUDE.md B.11): title, message, the
 *                optional abstraction note, and the optional fact id (facts are added in Milestone 6), with a
 *                Continue button. Modal: it blocks clicks on the rest of the UI until dismissed.
 * INVARIANTS:    onContinue runs exactly once, when the player presses Continue.
 * COLLABORATORS: UiGameListener (opens it), Notification, Stage, Skin.
 */
public final class NotificationDialog {
    private static final float WIDTH = 380;

    private NotificationDialog() {
    }

    /**
     * REQUIRES: all arguments non-null
     * MODIFIES: stage
     * EFFECTS:  Shows a modal dialog for n, centered on the stage. Pressing Continue closes it and runs onContinue.
     */
    public static Dialog show(Stage stage, Skin skin, Notification n, Runnable onContinue) {
        final Dialog dialog = new Dialog(n.title(), skin);
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.getTitleTable().padLeft(8);
        dialog.getContentTable().pad(12);

        Label message = new Label(n.message(), skin);
        message.setWrap(true);
        dialog.getContentTable().add(message).width(WIDTH).left().row();
        if (n.abstractionNote() != null) {
            Label note = new Label(n.abstractionNote(), skin, "small");
            note.setWrap(true);
            dialog.getContentTable().add(note).width(WIDTH).left().padTop(8).row();
        }
        if (n.factId() != null) {
            dialog.getContentTable().add(new Label("Fact: " + n.factId(), skin, "small")).left().padTop(8).row();
        }

        TextButton ok = new TextButton("Continue", skin);
        ok.addListener(new ChangeListener() {
            private boolean done;

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (done) {
                    return;
                }
                done = true;
                dialog.hide(null);
                onContinue.run();
            }
        });
        dialog.getButtonTable().add(ok).width(120).pad(8);
        dialog.show(stage, null); // no fade action: keeps the web build free of action pools
        dialog.setPosition(Math.round((stage.getWidth() - dialog.getWidth()) / 2f),
                Math.round((stage.getHeight() - dialog.getHeight()) / 2f));
        return dialog;
    }
}
