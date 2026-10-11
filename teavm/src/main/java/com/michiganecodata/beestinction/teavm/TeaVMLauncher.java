package com.michiganecodata.beestinction.teavm;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.michiganecodata.beestinction.BeestinctionGame;
import com.michiganecodata.beestinction.platform.NoOpLogUploader;

/**
 * Launches the TeaVM/HTML application. Based on the gdx-liftoff template.
 * Logs are not uploaded on the web until Milestone 5 (see FirestoreLogUploader).
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        //// If width and height are each greater than 0, then the app will use a fixed size.
        //config.width = 1280;
        //config.height = 800;
        //// If width and height are both 0, then the app will use all available space.
        config.width = 0;
        config.height = 0;
        new WebApplication(new BeestinctionGame(new NoOpLogUploader()), config);
    }
}
