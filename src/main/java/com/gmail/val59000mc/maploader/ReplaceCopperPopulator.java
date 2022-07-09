package com.gmail.val59000mc.maploader;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ReplaceCopperPopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk){

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 1; y < 20; y++){
                    Block block = chunk.getBlock(x, y, z);
                    Material type = block.getType();
                    if (type.equals(Material.DEEPSLATE)){
                        block.setType(Material.STONE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_COAL_ORE)){
                        block.setType(Material.COAL_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_IRON_ORE)){
                        block.setType(Material.IRON_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_COPPER_ORE)){
                        block.setType(Material.COPPER_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_GOLD_ORE)){
                        block.setType(Material.GOLD_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_REDSTONE_ORE)){
                        block.setType(Material.REDSTONE_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_EMERALD_ORE)){
                        block.setType(Material.EMERALD_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_LAPIS_ORE)){
                        block.setType(Material.LAPIS_ORE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_DIAMOND_ORE)){
                        block.setType(Material.DIAMOND_ORE,false);
                    }
                }
            }
        }
    }
}
