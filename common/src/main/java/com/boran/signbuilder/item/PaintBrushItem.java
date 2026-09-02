package com.boran.signbuilder.item;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();
        int selectedColor = 0;
        boolean isSmartFill = false;

        if (tag != null) {
            if (tag.contains("SelectedColor")) selectedColor = tag.getInt("SelectedColor");
            if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
        }

        Component label = Component.translatable("tooltip.signbuilder.selected_color");
        Component valueComponent;

        if (selectedColor == -1) {
            float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
            int dynamicColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            valueComponent = Component.translatable("color.signbuilder.rainbow").withStyle(Style.EMPTY.withColor(dynamicColor));
        } else if (selectedColor <= 15) {
            valueComponent = Component.translatable(getColorNameKey(selectedColor)).withStyle(Style.EMPTY.withColor(getActualHexColor(selectedColor)));
        } else {
            valueComponent = Component.literal("#" + String.format("%06X", selectedColor).toUpperCase()).withStyle(Style.EMPTY.withColor(selectedColor));
        }

        tooltipComponents.add(Component.empty().append(label).append(" ").append(valueComponent));

        if (tag != null && tag.contains("SelectedMaterial")) {
            String matKey = tag.getString("SelectedMaterial").replace("minecraft:", "");
            tooltipComponents.add(Component.translatable("tooltip.signbuilder.material").append(": ").append(Component.translatable("material.signbuilder." + matKey)).withStyle(ChatFormatting.GOLD));
        }

        tooltipComponents.add(Component.translatable("tooltip.signbuilder.brush.smart_fill").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                boolean isSmartFill = stack.getOrCreateTag().getBoolean("IsSmartFill");
                stack.getOrCreateTag().putBoolean("IsSmartFill", !isSmartFill);
                player.displayClientMessage(Component.translatable("message.signbuilder.brush.smart_fill_toggle").withStyle(ChatFormatting.YELLOW).append(Component.translatable(!isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(!isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, !isSmartFill ? 1.5F : 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.isClientSide()) EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.openPaintBrushScreen());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof LetterBlockEntity letterEntity) {
            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    int copiedColor = letterEntity.isRainbow() ? -1 : letterEntity.getRgbColor();
                    stack.getOrCreateTag().putInt("SelectedColor", copiedColor);
                    if (copiedColor == -1) player.displayClientMessage(Component.translatable("message.signbuilder.color_copied").append(" [Rainbow]").withStyle(Style.EMPTY.withColor(0xFF55FF)), true);
                    else player.displayClientMessage(Component.translatable("message.signbuilder.color_copied").append(" [#" + String.format("%06X", copiedColor).toUpperCase() + "]").withStyle(Style.EMPTY.withColor(copiedColor)), true);
                }
                if (player != null) level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                return InteractionResult.SUCCESS;
            }

            CompoundTag tag = stack.getOrCreateTag();
            int selectedColor = tag.contains("SelectedColor") ? tag.getInt("SelectedColor") : 0;
            boolean isSmartFill = tag.getBoolean("IsSmartFill");
            SignMaterial newMaterial = parseMaterial(tag.contains("SelectedMaterial") ? tag.getString("SelectedMaterial") : "minecraft:white_concrete");

            if (isSmartFill) {
                applyColorToConnected(level, pos, player, stack, context.getHand(), selectedColor, newMaterial);
            } else {
                SignMaterial oldMaterial = state.hasProperty(LetterBlock.MATERIAL) ? state.getValue(LetterBlock.MATERIAL) : SignMaterial.DEFAULT;

                if (!level.isClientSide() && player != null && !tryConsumeMaterial(player, oldMaterial, newMaterial)) {
                    player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                BlockState newState = state;
                if (state.hasProperty(LetterBlock.MATERIAL)) newState = state.setValue(LetterBlock.MATERIAL, newMaterial);
                if (selectedColor != -1 && selectedColor <= 15 && newState.hasProperty(ModBlocks.COLOR)) newState = newState.setValue(ModBlocks.COLOR, selectedColor);

                if (level.isClientSide()) {
                    BlockEntity updatedBe = level.getBlockEntity(pos);
                    if (updatedBe instanceof LetterBlockEntity be) {
                        be.setSavedMaterial(newMaterial);
                        if (selectedColor == -1) be.setRainbow(true);
                        else { be.setRainbow(false); be.setRgbColor(getActualHexColor(selectedColor)); }
                    }
                    level.setBlock(pos, newState, 11);
                    EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos));
                } else {
                    level.setBlock(pos, newState, 3);
                    BlockEntity updatedBe = level.getBlockEntity(pos);
                    if (updatedBe instanceof LetterBlockEntity be) {
                        be.setSavedMaterial(newMaterial);
                        if (selectedColor == -1) be.setRainbow(true);
                        else { be.setRainbow(false); be.setRgbColor(getActualHexColor(selectedColor)); }
                        be.setChanged();
                        level.sendBlockUpdated(pos, state, newState, 3);
                    }
                    if (player != null) {
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!player.isCreative()) stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.SUCCESS;
    }

    private void applyColorToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int selectedColor, SignMaterial newMaterial) {
        List<BlockPos> targets = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 256) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof LetterBlockEntity) {
                targets.add(current);
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && level.getBlockEntity(neighbor) instanceof LetterBlockEntity) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        int blocksPainted = 0;
        int failedMaterial = 0;
        int failedDurability = 0;
        int currentDamage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();

        for (BlockPos current : targets) {
            if (player != null && !player.isCreative() && (currentDamage + blocksPainted) >= maxDamage) {
                failedDurability++;
                continue;
            }

            BlockState currentState = level.getBlockState(current);
            SignMaterial oldMaterial = currentState.hasProperty(LetterBlock.MATERIAL) ? currentState.getValue(LetterBlock.MATERIAL) : SignMaterial.DEFAULT;

            if (!level.isClientSide() && player != null) {
                if (!tryConsumeMaterial(player, oldMaterial, newMaterial)) {
                    failedMaterial++;
                    continue;
                }
            }

            BlockState newState = currentState;
            if (currentState.hasProperty(LetterBlock.MATERIAL)) newState = currentState.setValue(LetterBlock.MATERIAL, newMaterial);
            if (selectedColor != -1 && selectedColor <= 15 && newState.hasProperty(ModBlocks.COLOR)) newState = newState.setValue(ModBlocks.COLOR, selectedColor);

            if (level.isClientSide()) {
                BlockEntity updatedBe = level.getBlockEntity(current);
                if (updatedBe instanceof LetterBlockEntity be) {
                    be.setSavedMaterial(newMaterial);
                    if (selectedColor == -1) be.setRainbow(true);
                    else { be.setRainbow(false); be.setRgbColor(getActualHexColor(selectedColor)); }
                }
                level.setBlock(current, newState, 11);
                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.setBlocksDirty(current));
            } else {
                level.setBlock(current, newState, 3);
                BlockEntity updatedBe = level.getBlockEntity(current);
                if (updatedBe instanceof LetterBlockEntity be) {
                    be.setSavedMaterial(newMaterial);
                    if (selectedColor == -1) be.setRainbow(true);
                    else { be.setRainbow(false); be.setRgbColor(getActualHexColor(selectedColor)); }
                    be.setChanged();
                    level.sendBlockUpdated(current, currentState, newState, 3);
                }
                blocksPainted++;
            }
        }

        if (player != null && !level.isClientSide()) {
            if (blocksPainted > 0) {
                level.playSound(null, startPos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) stack.hurtAndBreak(blocksPainted, player, (p) -> p.broadcastBreakEvent(hand));
            }
            if (failedMaterial > 0) player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.partial_material", blocksPainted, failedMaterial).withStyle(ChatFormatting.YELLOW), true);
            else if (failedDurability > 0) player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.partial_durability", blocksPainted, failedDurability).withStyle(ChatFormatting.YELLOW), true);
            else if (blocksPainted > 0) player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.success", blocksPainted).withStyle(ChatFormatting.GREEN), true);
        }
    }

    private boolean tryConsumeMaterial(Player player, SignMaterial oldMat, SignMaterial newMat) {
        if (player.isCreative() || oldMat == newMat) return true;
        if (newMat != SignMaterial.DEFAULT) {
            Item requiredItem = getItemForMaterial(newMat);
            if (countItemInInventory(player, requiredItem) < 1) return false;
            consumeItemFromInventory(player, requiredItem, 1);
        }
        if (oldMat != SignMaterial.DEFAULT) {
            ItemStack refundStack = new ItemStack(getItemForMaterial(oldMat), 1);
            if (!player.getInventory().add(refundStack)) player.drop(refundStack, false);
        }
        return true;
    }

    private Item getItemForMaterial(SignMaterial material) {
        String regName = switch (material) {
            case OAK -> "minecraft:oak_planks"; case SPRUCE -> "minecraft:spruce_planks"; case BIRCH -> "minecraft:birch_planks";
            case JUNGLE -> "minecraft:jungle_planks"; case ACACIA -> "minecraft:acacia_planks"; case DARK_OAK -> "minecraft:dark_oak_planks";
            case MANGROVE -> "minecraft:mangrove_planks"; case CHERRY -> "minecraft:cherry_planks"; case BAMBOO -> "minecraft:bamboo_planks";
            case IRON -> "minecraft:iron_block"; case ANDESITE -> "minecraft:polished_andesite"; default -> "minecraft:white_concrete";
        };
        return BuiltInRegistries.ITEM.get(new ResourceLocation(regName));
    }

    private int countItemInInventory(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() == item) count += s.getCount();
        }
        return count;
    }

    private void consumeItemFromInventory(Player player, Item item, int amount) {
        int amountLeft = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() == item) {
                if (s.getCount() >= amountLeft) { s.shrink(amountLeft); break; }
                else { amountLeft -= s.getCount(); s.setCount(0); }
            }
        }
    }

    private SignMaterial parseMaterial(String matString) {
        return switch (matString) {
            case "minecraft:oak_planks" -> SignMaterial.OAK; case "minecraft:spruce_planks" -> SignMaterial.SPRUCE; case "minecraft:birch_planks" -> SignMaterial.BIRCH;
            case "minecraft:jungle_planks" -> SignMaterial.JUNGLE; case "minecraft:acacia_planks" -> SignMaterial.ACACIA; case "minecraft:dark_oak_planks" -> SignMaterial.DARK_OAK;
            case "minecraft:mangrove_planks" -> SignMaterial.MANGROVE; case "minecraft:cherry_planks" -> SignMaterial.CHERRY; case "minecraft:bamboo_planks" -> SignMaterial.BAMBOO;
            case "minecraft:iron_block" -> SignMaterial.IRON; case "minecraft:polished_andesite" -> SignMaterial.ANDESITE; default -> SignMaterial.DEFAULT;
        };
    }

    private static String getColorNameKey(int index) {
        return switch (index) {
            case 0 -> "color.signbuilder.white"; case 1 -> "color.signbuilder.orange"; case 2 -> "color.signbuilder.magenta"; case 3 -> "color.signbuilder.light_blue";
            case 4 -> "color.signbuilder.yellow"; case 5 -> "color.signbuilder.lime"; case 6 -> "color.signbuilder.pink"; case 7 -> "color.signbuilder.gray";
            case 8 -> "color.signbuilder.light_gray"; case 9 -> "color.signbuilder.cyan"; case 10 -> "color.signbuilder.purple"; case 11 -> "color.signbuilder.blue";
            case 12 -> "color.signbuilder.brown"; case 13 -> "color.signbuilder.green"; case 14 -> "color.signbuilder.red"; case 15 -> "color.signbuilder.black"; default -> "color.signbuilder.white";
        };
    }

    public static int getActualHexColor(int colorValue) {
        if (colorValue > 15 || colorValue < -1) {
            return colorValue;
        }

        return switch (colorValue) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8; case 4 -> 0xE5E533; case 5 -> 0x7FCC19;
            case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C; case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0xCF2323; case 15 -> 0x191919; default -> 0xFFFFFF;
        };
    }
}