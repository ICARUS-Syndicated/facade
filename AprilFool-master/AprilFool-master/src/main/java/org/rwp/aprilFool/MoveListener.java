package org.rwp.aprilFool;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;x
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MoveListener implements Listener {
    public static final Map<Location,ReplacedBlock> replaced = new ConcurrentHashMap<>();
    private final Map<Player, Long> lastCheck = new HashMap<>();
    private static long COOLDOWN_MS = 1000; 
    
    private static final Random random = new Random();
    
    private static short replaceDistance = 16;
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        long now = System.currentTimeMillis();
        Long last = lastCheck.get(e.getPlayer());
        if (last != null && now - last < COOLDOWN_MS) {
            return;
        }
        Set<Block> blocks = getVisibleBlocks(e.getPlayer(),replaceDistance,110);
        for (Block block : blocks) {
            replaced.put(block.getLocation(),new ReplacedBlock(System.currentTimeMillis(),true));
            if(random.nextInt(99)<69) {
                block.setType(Material.AIR);
                block.setType(getRandomBlock());
            }
        }
    }
    public Set<Block> getVisibleBlocks(Player player, double maxDistance, double fovAngle) {
        Set<Block> visibleBlocks = new HashSet<>();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();

        // 遍历视锥范围内的方块
        for (double d = 1; d <= maxDistance; d += 0.5) {
            // 在距离 d 处的圆环上采样
            double radius = Math.tan(Math.toRadians(fovAngle / 2)) * d;
            int samplePoints = Math.max(4, (int) (radius * 8)); // 根据半径动态采样

            for (int i = 0; i < samplePoints; i++) {
                double angle = (2 * Math.PI * i) / samplePoints;
                double offsetX = Math.cos(angle) * radius;
                double offsetY = Math.sin(angle) * radius;

                // 计算世界坐标
                Vector offset = new Vector(offsetX, offsetY, 0)
                        .rotateAroundX(Math.toRadians(-eyeLoc.getPitch()))
                        .rotateAroundY(Math.toRadians(-eyeLoc.getYaw()));

                Location checkLoc = eyeLoc.clone().add(direction.clone().multiply(d)).add(offset);
                Block block = checkLoc.getBlock();

                if (!visibleBlocks.contains(block) && hasLineOfSight(eyeLoc, block)) {
                    if((block.getType() != Material.AIR ) && (block.getType() != Material.WATER) && (block.getType() != Material.LAVA) ) {
                        if(!replaced.containsKey(block.getLocation())) {
                            visibleBlocks.add(block);
                        }
                    }
                }
            }
        }
        return visibleBlocks;
    }

    private boolean hasLineOfSight(Location from, Block target) {
        Location to = target.getLocation().add(0.5, 0.5, 0.5);
        double distance = from.distance(to);
        Vector dir = to.toVector().subtract(from.toVector()).normalize();

        RayTraceResult result = from.getWorld().rayTraceBlocks(from, dir, distance, FluidCollisionMode.NEVER, true);
        return result == null || result.getHitBlock() == null || result.getHitBlock().equals(target);
    }
    private static Material getRandomBlock() {
        Material[] blocks = Arrays.stream(Material.values())
                .filter(Material::isBlock)           // 是可放置的方块
                .filter(m -> !m.isAir())            // 排除空气
                .filter(m -> !m.name().startsWith("LEGACY_"))

                // 排除流体
                .filter(m -> !m.name().endsWith("_WATER") && !m.name().endsWith("_LAVA"))
                .filter(m -> !m.equals(Material.WATER) && !m.equals(Material.LAVA))

                // 排除传送门类
                .filter(m -> !m.name().contains("PORTAL"))
                .filter(m -> !m.name().contains("GATEWAY"))

                // 排除命令方块/结构方块等
                .filter(m -> !m.name().contains("COMMAND"))
                .filter(m -> !m.name().contains("STRUCTURE"))
                .filter(m -> !m.name().contains("JIGSAW"))
                .filter(m -> !m.name().contains("BARRIER"))
                .filter(m -> !m.name().contains("LIGHT"))  // 光源方块

                // 排除刷怪笼/试刷怪笼
                .filter(m -> !m.name().equals("SPAWNER"))
                .filter(m -> !m.name().equals("TRIAL_SPAWNER"))

                // 排除床岩（不可获取）
                .filter(m -> !m.name().equals("BEDROCK"))

                // 排除末地折跃门/传送门框架
                .filter(m -> !m.name().equals("END_PORTAL_FRAME"))

                // 排除气泡柱
                .filter(m -> !m.name().equals("BUBBLE_COLUMN"))
                
                //排除tnt
                .filter(m -> !m.name().equals("TNT"))
                .toArray(Material[]::new);


        return blocks[random.nextInt(blocks.length)];
    }
    public MoveListener(short replaceDistance, Long coldDown) {
        this.replaceDistance = replaceDistance;
        this.COOLDOWN_MS = coldDown;
        
    }
}
