package com.boran.signbuilder.item;

import com.boran.signbuilder.block.BackplateBlock;
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
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
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

        boolean isBackplateBlock = state.getBlock() instanceof BackplateBlock;
        boolean isLetterBlock = state.getBlock() instanceof LetterBlock;

        if (!isBackplateBlock && !isLetterBlock) {
            return InteractionResult.PASS;
        }

        LetterBlockEntity letterEntity = (blockEntity instanceof LetterBlockEntity lbe) ? lbe : null;
        if (letterEntity == null) {
            return InteractionResult.PASS;
        }

        Direction clickedFace = context.getClickedFace();
        boolean isBackFace = determineIfBackFace(state, clickedFace, player);

        boolean targetBackplate = isBackplateBlock;
        if (isLetterBlock && letterEntity.hasBackplate()) {
            if (player != null && player.isShiftKeyDown()) {
                targetBackplate = true;
            } else if (isBackFace) {
                targetBackplate = true;
            }
        }

        if (player != null && player.isShiftKeyDown() && !targetBackplate && isLetterBlock) {
            if (!level.isClientSide()) {
                int copiedColor = letterEntity.isRainbow() ? -1 : letterEntity.getRgbColor();
                stack.getOrCreateTag().putInt("SelectedColor", copiedColor);
                if (copiedColor == -1) player.displayClientMessage(Component.translatable("message.signbuilder.color_copied").append(" [Rainbow]").withStyle(Style.EMPTY.withColor(0xFF55FF)), true);
                else player.displayClientMessage(Component.translatable("message.signbuilder.color_copied").append(" [#" + String.format("%06X", copiedColor).toUpperCase() + "]").withStyle(Style.EMPTY.withColor(copiedColor)), true);
            }
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        CompoundTag tag = stack.getOrCreateTag();
        int selectedColor = tag.contains("SelectedColor") ? tag.getInt("SelectedColor") : 0;
        boolean isSmartFill = tag.getBoolean("IsSmartFill");
        boolean hasMaterial = tag.contains("SelectedMaterial");
        SignMaterial newMaterial = hasMaterial ? parseMaterial(tag.getString("SelectedMaterial")) : null;

        SignMaterial currentMat;
        if (targetBackplate) {
            currentMat = isBackFace ? letterEntity.getBackplateBackMaterial() : letterEntity.getBackplateFrontMaterial();
        } else {
            currentMat = letterEntity.getSavedMaterial();
        }

        if (!hasMaterial && currentMat != SignMaterial.DEFAULT) {
            return InteractionResult.PASS;
        }

        if (isSmartFill) {
            applyColorToConnected(level, pos, player, stack, context.getHand(), selectedColor, newMaterial, hasMaterial, targetBackplate, isBackFace);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (hasMaterial) {
            if (!level.isClientSide() && player != null && !tryConsumeMaterial(player, currentMat, newMaterial)) {
                player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }

        if (!level.isClientSide()) {
            applyToEntity(letterEntity, targetBackplate, isBackFace, newMaterial, selectedColor);
            level.sendBlockUpdated(pos, state, state, 3);

            if (player != null) {
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
        } else {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private boolean determineIfBackFace(BlockState state, Direction clickedFace, @Nullable Player player) {
        Direction plateFrontNormal;
        if (state.getBlock() instanceof BackplateBlock) {
            plateFrontNormal = state.getValue(BackplateBlock.FACING);
        } else {
            AttachFace face = state.hasProperty(LetterBlock.FACE) ? state.getValue(LetterBlock.FACE) : AttachFace.WALL;
            Direction facing = state.hasProperty(LetterBlock.FACING) ? state.getValue(LetterBlock.FACING) : Direction.NORTH;
            plateFrontNormal = (face == AttachFace.FLOOR) ? facing.getCounterClockWise() : facing;
        }

        if (clickedFace == plateFrontNormal) {
            return false;
        }
        if (clickedFace == plateFrontNormal.getOpposite()) {
            return true;
        }

        if (player != null) {
            Vec3 look = player.getLookAngle();
            double dot = look.x * plateFrontNormal.getStepX() + look.z * plateFrontNormal.getStepZ();
            return dot >= 0;
        }
        return false;
    }

    private void applyToEntity(LetterBlockEntity entity, boolean targetBackplate, boolean isBackFace, @Nullable SignMaterial newMaterial, int selectedColor) {
        if (targetBackplate) {
            if (isBackFace) {
                if (newMaterial != null) {
                    entity.setBackplateBackMaterial(newMaterial);
                }
                if (newMaterial == null || newMaterial == SignMaterial.DEFAULT) {
                    if (selectedColor == -1) {
                        entity.setBackplateBackRainbow(true);
                    } else {
                        entity.setBackplateBackRainbow(false);
                        entity.setBackplateBackColor(getActualHexColor(selectedColor));
                    }
                }
            } else {
                if (newMaterial != null) {
                    entity.setBackplateFrontMaterial(newMaterial);
                }
                if (newMaterial == null || newMaterial == SignMaterial.DEFAULT) {
                    if (selectedColor == -1) {
                        entity.setBackplateFrontRainbow(true);
                    } else {
                        entity.setBackplateFrontRainbow(false);
                        entity.setBackplateFrontColor(getActualHexColor(selectedColor));
                    }
                }
            }
        } else {
            if (newMaterial != null) {
                entity.setSavedMaterial(newMaterial);
            }
            if (newMaterial == null || newMaterial == SignMaterial.DEFAULT) {
                if (selectedColor == -1) {
                    entity.setRainbow(true);
                } else {
                    entity.setRainbow(false);
                    entity.setRgbColor(getActualHexColor(selectedColor));
                }
            }
        }
        entity.setChanged();
        entity.sync();
    }

    private void applyColorToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int selectedColor, @Nullable SignMaterial newMaterial, boolean hasMaterial, boolean targetBackplate, boolean isBackFace) {
        List<BlockPos> targets = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 256) {
            BlockPos current = queue.poll();
            BlockState cs = level.getBlockState(current);
            if (cs.getBlock() instanceof LetterBlock || cs.getBlock() instanceof BackplateBlock) {
                targets.add(current);
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        BlockState ns = level.getBlockState(neighbor);
                        if (ns.getBlock() instanceof LetterBlock || ns.getBlock() instanceof BackplateBlock) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
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
            BlockEntity cbe = level.getBlockEntity(current);
            LetterBlockEntity letterEntity = (cbe instanceof LetterBlockEntity lbe) ? lbe : null;
            if (letterEntity == null) continue;

            SignMaterial currentMat;
            if (targetBackplate) {
                currentMat = isBackFace ? letterEntity.getBackplateBackMaterial() : letterEntity.getBackplateFrontMaterial();
            } else {
                currentMat = letterEntity.getSavedMaterial();
            }

            if (!hasMaterial && currentMat != SignMaterial.DEFAULT) {
                continue;
            }

            if (hasMaterial) {
                if (!level.isClientSide() && player != null && !tryConsumeMaterial(player, currentMat, newMaterial)) {
                    failedMaterial++;
                    continue;
                }
            }

            if (!level.isClientSide()) {
                applyToEntity(letterEntity, targetBackplate, isBackFace, newMaterial, selectedColor);
                level.sendBlockUpdated(current, currentState, currentState, 3);
                blocksPainted++;
            } else {
                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.setBlocksDirty(current));
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
            case IRON -> "minecraft:iron_block"; case ANDESITE -> "minecraft:polished_andesite";
            case GOLD -> "minecraft:gold_block"; case DIAMOND -> "minecraft:diamond_block"; case LAPIS -> "minecraft:lapis_block";
            case SMOOTH_STONE -> "minecraft:smooth_stone"; case POLISHED_DIORITE -> "minecraft:polished_diorite";
            case BRICKS -> "minecraft:bricks"; case STONE_BRICKS -> "minecraft:stone_bricks";
            default -> "minecraft:white_concrete";
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
            case "minecraft:iron_block" -> SignMaterial.IRON; case "minecraft:polished_andesite" -> SignMaterial.ANDESITE;
            case "minecraft:gold_block" -> SignMaterial.GOLD; case "minecraft:diamond_block" -> SignMaterial.DIAMOND; case "minecraft:lapis_block" -> SignMaterial.LAPIS;
            case "minecraft:smooth_stone" -> SignMaterial.SMOOTH_STONE; case "minecraft:polished_diorite" -> SignMaterial.POLISHED_DIORITE;
            case "minecraft:bricks" -> SignMaterial.BRICKS; case "minecraft:stone_bricks" -> SignMaterial.STONE_BRICKS;
            default -> SignMaterial.DEFAULT;
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