package net.megavex.scoreboardlibrary.implementation;

import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.implementation.packetAdapter.PacketAdapterProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public final class PacketAdapterLoader {
  private static final String MODERN = "modern",
    LEGACY = "legacy",
    PACKET_EVENTS = "packetevents";

  private PacketAdapterLoader() {
  }

  public static @NotNull PacketAdapterProvider loadPacketAdapter() throws NoPacketAdapterAvailableException {
    Class<?> nmsClass = findAndLoadImplementationClass();
    if (nmsClass == null) {
      throw new NoPacketAdapterAvailableException();
    }

    try {
      return (PacketAdapterProvider) nmsClass.getConstructors()[0].newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("couldn't initialize packet adapter", e);
    }
  }

  private static @Nullable Class<?> findAndLoadImplementationClass() {
    String version = Bukkit.getServer().getBukkitVersion();
    int dashIndex = version.indexOf('-');
    if (dashIndex != -1) {
      version = version.substring(0, dashIndex);
    }

    Class<?> nmsClass = tryLoadVersion(version);
    if (nmsClass != null) {
      return nmsClass;
    }

    return tryLoadPacketEvents();
  }

  private static @Nullable Class<?> tryLoadVersion(@NotNull String serverVersion) {
    // https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-legacy/
    // https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-10-1-15/
    // https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-16/
    // https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-21/
    switch (serverVersion) {
      case "1.7.10":
      case "1.8":
      case "1.8.3":
      case "1.8.4":
      case "1.8.5":
      case "1.8.6":
      case "1.8.7":
      case "1.8.8":
      case "1.9":
      case "1.9.2":
      case "1.9.4":
      case "1.10.2":
      case "1.11":
      case "1.11.2":
      case "1.12":
      case "1.12.1":
      case "1.12.2":
        return tryLoadImplementationClass(LEGACY);
      case "1.17":
      case "1.17.1":
      case "1.18":
      case "1.18.1":
      case "1.18.2":
      case "1.19":
      case "1.19.1":
      case "1.19.2":
      case "1.19.3":
      case "1.19.4":
      case "1.20":
      case "1.20.1":
      case "1.20.2":
      case "1.20.3":
      case "1.20.4":
      case "1.20.5":
      case "1.20.6":
      case "1.21":
      case "1.21.1":
      case "1.21.2":
      case "1.21.3":
      case "1.21.4":
      case "1.21.5":
      case "1.21.6":
      case "1.21.7":
      case "1.21.8":
        return tryLoadImplementationClass(MODERN);
      default:
        // Hide from relocation checkers
        String property = "net.mega".concat("vex.scoreboardlibrary.forceModern");
        if (System.getProperty(property, "").equalsIgnoreCase("true")) {
          return tryLoadImplementationClass(MODERN);
        }

        return null;
    }
  }

  private static @Nullable Class<?> tryLoadPacketEvents() {
    Class<?> nmsClass = tryLoadImplementationClass(PACKET_EVENTS);
    if (nmsClass == null) {
      return null;
    }

    try {
      Class.forName("com.github.retrooper.packetevents.PacketEvents");
      return nmsClass;
    } catch (ClassNotFoundException ignored) {
      return null;
    }
  }

  private static @Nullable Class<?> tryLoadImplementationClass(@NotNull String name) {
    try {
      String path = "net.megavex.scoreboardlibrary.implementation.packetAdapter." + name + ".PacketAdapterProviderImpl";
      return Class.forName(path);
    } catch (ClassNotFoundException ignored) {
      return null;
    }
  }
}
