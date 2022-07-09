package com.gmail.val59000mc.maploader;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ReplaceDeepslatePopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk){

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 1; y < 97; y++){
                    Block block = chunk.getBlock(x, y, z);
                    Material type = block.getType();
                    if (type.equals(Material.COPPER_ORE)){
                        block.setType(Material.STONE,false);
                    }
                    if (type.equals(Material.DEEPSLATE_COPPER_ORE)){
                        block.setType(Material.STONE,false);
                    }
                }
            }
        }
    }
}
