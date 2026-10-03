# Smarter Moving

Smarter Moving 1.0.0 is a Minecraft 1.12.2 mod for Forge 14.23.5.2854. It adds climbing, crawling, sliding, swimming, diving, faster sprinting, jump variants, and movement animations.

## Install

Install Forge 14.23.5.2854 for Minecraft 1.12.2, then put `SmarterMoving-1.12.2-1.0.0.jar` in the `mods` folder. Use the same JAR on a dedicated server. PlayerAPI, RenderPlayerAPI, and SmartRender are no longer required.

## Build

Use Java 8 and run `./gradlew clean build` (or `gradlew.bat clean build` on Windows). The distributable JAR is written to `build/libs/`. The build embeds the Mixin runtime needed by the Forge 1.12.2 launch environment.

`build.properties` is the single source for the mod version. Pushes and pull requests run the GitHub Actions build. A pushed tag matching `v{version}` (for example, `v1.0.0`) publishes a GitHub Release with the matching JAR after a successful build. Update `build.properties` before tagging the next version.

## Configuration

The mod creates `smart_moving_options.txt` next to Minecraft's `options.txt` on first launch. Edit it to configure movement behavior.

## Credits and migration

This project continues [Smart Moving Reboot](https://github.com/doch2/SmartMovingReboot). Original Smart Moving work was by Divisor, with later ports and contributions by JonnyNova, elveskevtar, doch13_, and others. See [MIGRATION.md](MIGRATION.md) for the dependency-removal architecture and current runtime verification status. The [Korean README](README_Korean.md) is also available.
