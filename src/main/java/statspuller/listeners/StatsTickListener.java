package statspuller.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import statspuller.gui.StatsPlayerListGui;

public class StatsTickListener {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        StatsPlayerListGui gui = new StatsPlayerListGui(Minecraft.getMinecraft(), Minecraft.getMinecraft().ingameGUI);
        ReflectionHelper.setPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, gui, "field_175196_v", "overlayPlayerList", "v");
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
