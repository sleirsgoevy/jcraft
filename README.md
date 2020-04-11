# JCraft

This is a work-in-progress Minecraft clone written in pure Java. The core engine only uses the core Java library (bootclasspath), so it should be highly portable. The default wrapper (src/Main.java, implements display/keyboard/mouse access) uses Java AWT.

## Building

Run `make`. The `src.jar` file will appear in the current directory.

## Current status

Currently the game is able to render single-color blocks with outlines. Texture support is planned.
