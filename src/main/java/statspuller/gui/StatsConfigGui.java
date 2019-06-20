package statspuller.gui;

import statspuller.StatsPuller;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class StatsConfigGui extends GuiConfig {
    public StatsConfigGui(GuiScreen parentScreen) {
        super(parentScreen,
                new ConfigElement(StatsPuller.config.getCategory(Configuration.CATEGORY_CLIENT)).getChildElements(),
                StatsPuller.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(StatsPuller.config.toString()));
    }
}
