/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

public class Plugin extends de.timesnake.basic.game.util.user.Plugin {

  public static final Plugin GRAFFITI = new Plugin("Graffiti", "GGF");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
