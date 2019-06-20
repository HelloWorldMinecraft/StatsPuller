package statspuller.servers.hypixel.backend;

import statspuller.Backend;
import statspuller.Utils;
import me.kbrewster.hypixelapi.player.stats.tnt.TNTGames;
import org.apache.commons.lang3.StringUtils;
import statspuller.servers.hypixel.HypixelBackend;

import javax.annotation.Nonnull;

public class TNTWizardsBackend extends HypixelBackend<TNTGames> {

    public TNTWizardsBackend() {
        super(TNTGames.class);
    }

    @Override
    public String getName() {
        return "TNT Wizards";
    }

    @Override
    public long getKills(@Nonnull TNTGames tntGames) {
        return tntGames.getKillsCapture();
    }

    @Override
    public long getDeaths(@Nonnull TNTGames tntGames) {
        return tntGames.getDeathsCapture();
    }

    @Override
    public long getWins(@Nonnull TNTGames game) {
        return game.getWinsCapture();
    }

    @Override
    public String getExtra(@Nonnull TNTGames games) {
        String className = games.getWizardsSelectedClass();
        if (className == null) return "None";

        String name = StringUtils.capitalize(className.replace("new_", "").replace("wizard", ""));

        String prefix;
        if (name.contains("Fire")) prefix = "§6";
        else if (name.contains("Ice")) prefix = "§b";
        else if (name.contains("Wither")) prefix = "§8";
        else if (name.contains("Kinetic")) prefix = "§f";
        else if (name.contains("Toxic")) prefix = "§e";
        else if (name.contains("Blood")) prefix = "§c";
        else prefix = "§d";

        if (getKills(games) >= 10000) prefix += "§l";

        return prefix + name.trim();
    }

    @Override
    public String getDump(@Nonnull TNTGames game) {
        return "Total Wins: " + game.getWinsCapture() + "\n" +
                "Total Kills: " + game.getKillsCapture() + "\n" +
                "Total Deaths: " + game.getDeathsCapture() + "\n" +
                "Total Assists: " + game.getAssistsCapture() + "\n" +
                "\n" +
                "Total Kills (Wither): " + game.getNewWitherwizardKills() + "\n" +
                "Total Kills (Toxic): " + game.getNewToxicwizardKills() + "\n" +
                "\n" +
                "Upgrades (Fire): " + game.getNewFirewizardExplode() + " : " + game.getNewFirewizardRegen() + "\n" +
                "Upgrades (Ice): " + game.getNewIcewizardExplode() + " : " + game.getNewIcewizardRegen() + "\n" +
                "Upgrades (Kinetic): " + game.getNewKineticwizardExplode() + " : " + game.getNewKineticwizardRegen() + "\n" +
                "Upgrades (Wither): " + game.getNewWitherwizardExplode() + " : " + game.getNewWitherwizardRegen() + "\n" +
                "Upgrades (Toxic): " + game.getNewToxicwizardExplode() + " : " + game.getNewToxicwizardRegen() + "\n" +
                "\n" +
                "Class: " + game.getCaptureClass() + "\n" +
                "Selected Class: " + game.getWizardsSelectedClass() + "\n" +
                "KDR: " + (game.getKillsCapture() / game.getDeathsCapture());
    }
}
