package dev.privateclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds storage block entities and target ores around the player (or the freecam)
 * a few times per second. The renderer just draws whatever is in {@link #hits()}.
 */
public final class ScanCache {
    public record Hit(BlockPos pos, int argb) {}

    private static final int MAX_HITS = 4000;
    private static final int SCAN_EVERY_TICKS = 10;

    private static volatile List<Hit> hits = List.of();
    private static int cooldown = 0;

    public static List<Hit> hits() {
        return hits;
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || (!Config.storageEsp && !Config.oreEsp)) {
            hits = List.of();
            return;
        }
        if (--cooldown > 0) return;
        cooldown = SCAN_EVERY_TICKS;

        Vec3 c = Config.freecam ? Freecam.position() : mc.player.position();
        double r = Config.range;
        double r2 = r * r;

        int cx0 = Mth.floor(c.x - r) >> 4;
        int cx1 = Mth.floor(c.x + r) >> 4;
        int cz0 = Mth.floor(c.z - r) >> 4;
        int cz1 = Mth.floor(c.z + r) >> 4;

        List<Hit> out = new ArrayList<>();
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;

                if (Config.storageEsp) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be.isRemoved()) continue;
                        int color = storageColor(be);
                        if (color != 0 && be.getBlockPos().distToCenterSqr(c.x, c.y, c.z) <= r2) {
                            out.add(new Hit(be.getBlockPos(), color));
                        }
                    }
                }
                if (Config.oreEsp) {
                    scanOres(chunk, cx, cz, c, r2, out);
                }
                if (out.size() >= MAX_HITS) break;
            }
        }
        hits = out;
    }

    private static void scanOres(LevelChunk chunk, int cx, int cz, Vec3 c, double r2, List<Hit> out) {
        LevelChunkSection[] sections = chunk.getSections();
        int minSection = chunk.getMinSectionY();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section.hasOnlyAir() || !section.maybeHas(state -> oreColor(state.getBlock()) != 0)) continue;

            int baseX = cx << 4;
            int baseY = (minSection + i) << 4;
            int baseZ = cz << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int color = oreColor(section.getBlockState(x, y, z).getBlock());
                        if (color == 0) continue;
                        BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                        if (pos.distToCenterSqr(c.x, c.y, c.z) <= r2) {
                            out.add(new Hit(pos, color));
                        }
                    }
                }
            }
        }
    }

    private static int oreColor(Block b) {
        if (Config.ancientDebris && b == Blocks.ANCIENT_DEBRIS) return 0xFFFF8C28;
        if (Config.diamondOre && b == Blocks.DIAMOND_ORE) return 0xFF55FFFF;
        if (Config.deepslateDiamond && b == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF00D7D7;
        if (Config.deepslateEmerald && b == Blocks.DEEPSLATE_EMERALD_ORE) return 0xFF33FF66;
        return 0;
    }

    private static int storageColor(BlockEntity be) {
        if (be instanceof EnderChestBlockEntity) return 0xFFB05CFF;
        if (be instanceof TrappedChestBlockEntity) return 0xFFFF5555;
        if (be instanceof ChestBlockEntity) return 0xFFFFB000;
        if (be instanceof BarrelBlockEntity) return 0xFFC08040;
        if (be instanceof ShulkerBoxBlockEntity) return 0xFFFF55FF;
        return 0;
    }

    private ScanCache() {}
}
