package al3ncar.al3hg.utils;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

public class MobsControillers {
    public void ControlerMax(){
        World world = Bukkit.getWorld("hgmapa");
        world.setTime(12000);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }

}
