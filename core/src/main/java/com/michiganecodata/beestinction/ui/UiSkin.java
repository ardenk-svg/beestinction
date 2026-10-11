package com.michiganecodata.beestinction.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * OVERVIEW:      Builds the placeholder Scene2D Skin in code from libGDX's default uiskin images and font
 *                (assets/ui, Apache-2.0). DECISION: building the styles in code (instead of loading uiskin.json) avoids
 *                reflection-based JSON mapping, which CLAUDE.md A.5 rule 6 rules out and which the web build would
 *                need reflection metadata for. The humans will design the real look.
 *                Style names: Label "default", "title", "small", "warning"; TextButton "default", "toggle";
 *                TextField "default"; Window "default"; ScrollPane "default"; Drawable "panel".
 * INVARIANTS:    Stateless factory.
 * COLLABORATORS: GameServices (owns the skin), every UI class.
 */
public final class UiSkin {
    public static final Color HONEY = new Color(1f, 0.78f, 0.2f, 1f);

    private UiSkin() {
    }

    /**
     * REQUIRES: Gdx.files and a GL context are available
     * MODIFIES: nothing
     * EFFECTS:  Returns a new Skin. The caller disposes it (which also disposes the atlas and font).
     */
    public static Skin create() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas"));
        Skin skin = new Skin(atlas);
        BitmapFont font = new BitmapFont(Gdx.files.internal("ui/default.fnt"), atlas.findRegion("default"));
        font.setUseIntegerPositions(true);
        skin.add("default-font", font);

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        skin.add("title", new Label.LabelStyle(font, HONEY));
        skin.add("small", new Label.LabelStyle(font, new Color(0.8f, 0.8f, 0.8f, 1f)));
        skin.add("warning", new Label.LabelStyle(font, new Color(1f, 0.55f, 0.45f, 1f)));

        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle(
                skin.getDrawable("default-round"), skin.getDrawable("default-round-down"), null, font);
        button.over = skin.newDrawable("default-round", new Color(0.85f, 0.85f, 0.85f, 1f));
        button.disabled = skin.newDrawable("default-round", new Color(0.45f, 0.45f, 0.45f, 1f));
        button.fontColor = Color.WHITE;
        button.disabledFontColor = Color.GRAY;
        skin.add("default", button);

        TextButton.TextButtonStyle toggle = new TextButton.TextButtonStyle(button);
        toggle.checked = skin.newDrawable("default-round", HONEY);
        toggle.checkedFontColor = Color.BLACK;
        skin.add("toggle", toggle);

        TextField.TextFieldStyle field = new TextField.TextFieldStyle(font, Color.WHITE,
                skin.getDrawable("cursor"), skin.getDrawable("selection"), skin.getDrawable("textfield"));
        skin.add("default", field);

        Window.WindowStyle window = new Window.WindowStyle(font, HONEY, skin.getDrawable("default-window"));
        window.stageBackground = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.45f));
        skin.add("default", window);

        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        scroll.vScroll = skin.getDrawable("default-scroll");
        scroll.vScrollKnob = skin.getDrawable("default-round-large");
        scroll.hScroll = skin.getDrawable("default-scroll");
        scroll.hScrollKnob = skin.getDrawable("default-round-large");
        skin.add("default", scroll);

        skin.add("panel", skin.newDrawable("white", new Color(0.07f, 0.07f, 0.09f, 0.88f)), Drawable.class);
        return skin;
    }
}
