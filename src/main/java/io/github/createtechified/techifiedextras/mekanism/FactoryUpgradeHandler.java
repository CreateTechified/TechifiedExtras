package io.github.createtechified.techifiedextras.mekanism;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static io.github.createtechified.techifiedextras.TechifiedExtrasMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactoryUpgradeHandler {
    private record TierStep(String fromPrefix, String toPrefix, ResourceLocation upgradeItem) {}

    public static final List<TierStep> steps = List.of(
            new TierStep("mekanism_extras:absolute_", "emextras:absolute_overclocked_", rl("evolvedmekanism", "overclocked_tier_installer")),
            new TierStep("emextras:absolute_overclocked_", "mekanism_extras:supreme_", rl("mekanism_extras", "supreme_tier_installer")),
            new TierStep("mekanism_extras:supreme_", "emextras:supreme_quantum_", rl("evolvedmekanism", "quantum_tier_installer")),
            new TierStep("emextras:supreme_quantum_", "mekanism_extras:cosmic_", rl("mekanism_extras", "cosmic_tier_installer")),
            new TierStep("mekanism_extras:cosmic_", "emextras:cosmic_dense_", rl("evolvedmekanism", "dense_tier_installer")),
            new TierStep("emextras:cosmic_dense_", "mekanism_extras:infinite_", rl("mekanism_extras", "infinite_tier_installer")),
            new TierStep("mekanism_extras:infinite_", "emextras:infinite_multiversal_", rl("evolvedmekanism", "multiversal_tier_installer")),
            new TierStep("emextras:infinite_multiversal_", "evolvedmekanism:creative_", rl("evolvedmekanism", "creative_tier_installer")),
            // alloying factory stuffs
            new TierStep("emextras:absolute_alloying_", "emextras:absolute_overclocked_alloying_", rl("evolvedmekanism", "overclocked_tier_installer")),
            new TierStep("emextras:absolute_overclocked_alloying_", "emextras:supreme_alloying_", rl("mekanism_extras", "supreme_tier_installer")),
            new TierStep("emextras:supreme_alloying_", "emextras:supreme_quantum_alloying_", rl("evolvedmekanism", "quantum_tier_installer")),
            new TierStep("emextras:supreme_quantum_alloying_", "emextras:cosmic_alloying_", rl("mekanism_extras", "cosmic_tier_installer")),
            new TierStep("emextras:cosmic_alloying_", "emextras:cosmic_dense_alloying_", rl("evolvedmekanism", "dense_tier_installer")),
            new TierStep("emextras:cosmic_dense_alloying_", "emextras:infinite_alloying_", rl("mekanism_extras", "infinite_tier_installer")),
            new TierStep("emextras:infinite_alloying_", "emextras:infinite_multiversal_alloying_", rl("evolvedmekanism", "multiversal_tier_installer")),
            // allow evmek to emextras
            new TierStep("evolvedmekanism:overclocked_", "emextras:absolute_overclocked_", rl("mekanism_extras", "absolute_tier_installer")),
            new TierStep("evolvedmekanism:quantum_", "emextras:supreme_quantum_", rl("mekanism_extras", "supreme_tier_installer")),
            new TierStep("evolvedmekanism:dense_", "emextras:cosmic_dense_", rl("mekanism_extras", "cosmic_tier_installer")),
            new TierStep("evolvedmekanism:multiversal_", "emextras:infinite_multiversal_", rl("mekanism_extras", "infinite_tier_installer"))
            );

    private static ResourceLocation rl(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!player.isShiftKeyDown()) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held.isEmpty()) return;
        System.out.println("[FactoryUpgradeHandler] Right-click block event fired!");

        for (TierStep step : steps) {
            Item upgradeItem = ForgeRegistries.ITEMS.getValue(step.upgradeItem);
            if (upgradeItem == null || !held.getItem().equals(upgradeItem)) continue;

            System.out.println("[FactoryUpgradeHandler] Player: " + player.getName().getString());
            System.out.println("[FactoryUpgradeHandler] Clicked block: " + ForgeRegistries.BLOCKS.getKey(block));
            System.out.println("[FactoryUpgradeHandler] Held item: " + ForgeRegistries.ITEMS.getKey(held.getItem()));

            String[] fromParts = step.fromPrefix.split(":", 2);
            if (fromParts.length != 2) continue;
            String fromNs = fromParts[0];
            String fromPrefix = fromParts[1];

            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
            if (blockId == null) continue;

            if (!blockId.getNamespace().equals(fromNs)) continue;
            if (!blockId.getPath().startsWith(fromPrefix) || !blockId.getPath().endsWith("_factory")) continue;

            String[] toParts = step.toPrefix.split(":", 2);
            if (toParts.length != 2) continue;
            String targetNs = toParts[0];
            String targetPrefix = toParts[1];

            String oldPath = blockId.getPath();
            if (!oldPath.startsWith(fromPrefix)) continue;
            String targetPath = targetPrefix + oldPath.substring(fromPrefix.length());

            System.out.println("[FactoryUpgradeHandler] Trying to find block: " + targetNs + ":" + targetPath);
            Block targetBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(targetNs, targetPath));
            if (targetBlock == null || targetBlock == Blocks.AIR) continue;

            BlockEntity oldTE = level.getBlockEntity(pos);
            CompoundTag nbt = null;
            if (oldTE != null) nbt = oldTE.saveWithFullMetadata();

            BlockState oldState = level.getBlockState(pos);
            BlockState newState = targetBlock.defaultBlockState();

            for (var property : oldState.getProperties()) {
                if (newState.hasProperty(property)) {
                    try {
                        newState = newState.setValue((Property) property, oldState.getValue(property));
                    } catch (Exception ignored) {}
                }
            }

            level.setBlock(pos, newState, 3);

            if (nbt != null) {
                BlockEntity newTE = level.getBlockEntity(pos);
                if (newTE != null) newTE.load(nbt);
            }

            if (!player.isCreative()) held.shrink(1);

            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            break;
        }
    }
}
