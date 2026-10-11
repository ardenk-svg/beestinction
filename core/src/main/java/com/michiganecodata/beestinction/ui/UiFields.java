package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * OVERVIEW:      Small helpers shared by the placeholder widgets: number fields and buttons with click callbacks.
 *                (Anonymous listener classes are libGDX's callback idiom, not data-variety inheritance.)
 * INVARIANTS:    Stateless.
 * COLLABORATORS: The ui widgets and screens.
 */
public final class UiFields {
    private UiFields() {
    }

    /**
     * REQUIRES: skin != null
     * MODIFIES: nothing
     * EFFECTS:  Returns a text field that accepts digits only, pre-filled with `initial`.
     */
    public static TextField numberField(Skin skin, int initial) {
        TextField f = new TextField(Integer.toString(initial), skin);
        f.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        f.setMaxLength(6);
        return f;
    }

    /**
     * REQUIRES: f != null
     * MODIFIES: nothing
     * EFFECTS:  Returns the field's value as an int, or -1 if it is empty or not a number.
     */
    public static int parse(TextField f) {
        String text = f.getText();
        if (text == null || text.isEmpty() || text.length() > 9) {
            return -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return -1;
            }
        }
        return Integer.parseInt(text);
    }

    /**
     * REQUIRES: skin, text, onClick != null
     * MODIFIES: nothing
     * EFFECTS:  Returns a button that runs onClick when pressed.
     */
    public static TextButton button(Skin skin, String text, Runnable onClick) {
        return button(skin, text, "default", onClick);
    }

    /**
     * REQUIRES: skin, text, style, onClick != null
     * MODIFIES: nothing
     * EFFECTS:  Returns a button with the given TextButton style that runs onClick when pressed.
     */
    public static TextButton button(Skin skin, String text, String style, Runnable onClick) {
        TextButton b = new TextButton(text, skin, style);
        b.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return b;
    }
}
