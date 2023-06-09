package xyz.spaceio.customoregen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneratorConfig {
    public List<GeneratorItem> itemList = new ArrayList<GeneratorItem>();
    public String permission = ";";
    public String label;
    public int unlock_islandLevel = 0;

    public GeneratorItem getRandomItem() {
        Random random = new Random();
        double d = random.nextDouble() * 100;
        for (GeneratorItem key : this.itemList) {
            if ((d -= key.getChance()) < 0)
                return key;
        }
        return new GeneratorItem("COBBLESTONE", (byte) 0, 0); // DEFAULT
    }
}
