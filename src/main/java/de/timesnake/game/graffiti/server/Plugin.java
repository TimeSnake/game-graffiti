/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.library.basic.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.game.util.user.Plugin {

    public static final Plugin GRAFFITI = new Plugin("Graffiti", "GGF", LogHelper.getLogger("Graffiti", Level.INFO));

    protected Plugin(String name, String code, Logger logger) {
        super(name, code, logger);
    }
}
