package statspuller.listeners;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import statspuller.StatsPuller;

public class StatsChatListener {

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();
        // §aYour new API key is §r§b896bc8a8-aff6-424b-a5e7-cd7227b44055§r

        if (!message.matches("§aYour new API key is §r§b[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}§r")) return;

        String key = message.split("§b")[1].split("§r")[0];

        StatsPuller.apiProperty.set(key);
        StatsPuller.config.save();
        StatsPuller.syncConfig();
    }
}
