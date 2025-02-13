package io.github.satxm.mcwifipnp;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(MCWiFiPnP.MODID)
public class MCWiFiPnP {
  public static final String MODID = "mcwifipnp";

  public MCWiFiPnP(IEventBus modEventBus) {
    NeoForge.EVENT_BUS.register(this);
    NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    NeoForge.EVENT_BUS.addListener((final ScreenEvent.Init.Post evt) -> {
      onAfterInitScreen(evt.getScreen().getMinecraft(), evt.getScreen(), evt.getListenersList(), evt::addListener, evt::removeListener);
    });
  }

  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {
    MCWiFiPnPUnit.ReadingConfig(event.getServer());
  }

  @SubscribeEvent
  public void onRegisterCommands(RegisterCommandsEvent event) {
    DeOpCommands.register(event.getDispatcher());
    OpCommand.register(event.getDispatcher());
    WhitelistCommand.register(event.getDispatcher());
    BanIpCommands.register(event.getDispatcher());
    BanListCommands.register(event.getDispatcher());
    BanPlayerCommands.register(event.getDispatcher());
    PardonCommand.register(event.getDispatcher());
    PardonIpCommand.register(event.getDispatcher());
    ForceOfflineCommand.register(event.getDispatcher());
  }

  @SubscribeEvent
  public void onServerStopping(ServerStoppingEvent event) {
    MCWiFiPnPUnit.CloseUPnPPort(event.getServer());
  }

  public static void onAfterInitScreen(Minecraft client, Screen screen, List<GuiEventListener> children,
      Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
    if (screen instanceof PauseScreen pauseScreen && screen.getClass() == PauseScreen.class) {
      if (pauseScreen.showsPauseMenu()) {
        findButton(children).ifPresent(button -> {
          Button newButton = Button.builder(Component.translatable("menu.shareToLan"), $ -> {
            client.setScreen(new ShareToLanScreenNew(screen));
          }).bounds(button.getX(), button.getY(), button.getWidth(), button.getHeight()).build();
          newButton.active = button.active;
          remove.accept(button);
          add.accept(newButton);
        });
      }
    }
  }

  private static Optional<Button> findButton(List<GuiEventListener> widgets) {
    for (GuiEventListener widget : widgets) {
      if (widget instanceof Button button) {
        if (button.getMessage().equals(Component.translatable("menu.shareToLan")))
          return Optional.of(button);
      }
    }
    return Optional.empty();
  }

}
