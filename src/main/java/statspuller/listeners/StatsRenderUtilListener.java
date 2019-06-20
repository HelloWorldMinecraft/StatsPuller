package statspuller.listeners;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//Literally disables the crosshair if in F5 and quake pro
public class StatsRenderUtilListener {
    @SubscribeEvent
    public void onDrawCrosshair(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;
        if (Minecraft.getMinecraft().gameSettings.fovSetting == 70.0) return;
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) return;

        event.setCanceled(true);
    }
}
