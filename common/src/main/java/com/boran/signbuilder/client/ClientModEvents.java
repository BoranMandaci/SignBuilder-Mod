package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.grid.GridModeManager;
import com.boran.signbuilder.client.grid.GridRaytrace;
import com.boran.signbuilder.client.render.GridSignBlockEntityRenderer;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.PlaceGridLetterC2SPacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class ClientModEvents {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        GridModeManager.init();
        ClientTickEvent.CLIENT_POST.register(HeldButtonController::tick);
        ClientTickEvent.CLIENT_POST.register(com.boran.signbuilder.client.render.SignBuilderBakedModel::checkAndWrap);

        dev.architectury.event.events.client.ClientRawInputEvent.MOUSE_SCROLLED.register((client, amount) -> {
            if (onMouseScrolled(0.0, amount)) {
                return dev.architectury.event.EventResult.interruptFalse();
            }
            return dev.architectury.event.EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) {
                Minecraft client = Minecraft.getInstance();
                HitResult rawHit = client.hitResult;
                if (rawHit instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                    ItemStack held = player.getItemInHand(hand);
                    if (player.isShiftKeyDown() && (held.is(net.minecraft.world.item.Items.GLOW_INK_SAC) || held.is(net.minecraft.world.item.Items.INK_SAC))) {
                        net.minecraft.world.level.block.entity.BlockEntity be = client.level.getBlockEntity(hit.getBlockPos());
                        boolean hasBackplate = false;
                        if (be instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                            com.boran.signbuilder.block.entity.LetterBlockEntity master = com.boran.signbuilder.block.LetterBlock.findMaster(client.level, hit.getBlockPos(), lbe);
                            hasBackplate = master != null && master.hasBackplate();
                        } else if (be instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                            hasBackplate = gbe.hasBackplate();
                        }
                        if (hasBackplate) {
                            net.minecraft.world.phys.Vec3 loc = hit.getLocation();
                            com.boran.signbuilder.network.ModMessages.sendToServer(
                                    new com.boran.signbuilder.network.ApplyBackplateInkC2SPacket(
                                            hit.getBlockPos(), hit.getDirection(), loc.x, loc.y, loc.z, hand == InteractionHand.MAIN_HAND
                                    )
                            );
                            player.swing(hand);
                            return EventResult.interruptFalse();
                        }
                    }

                    boolean holdingInk = player.getMainHandItem().is(net.minecraft.world.item.Items.GLOW_INK_SAC) ||
                            player.getMainHandItem().is(net.minecraft.world.item.Items.INK_SAC) ||
                            player.getOffhandItem().is(net.minecraft.world.item.Items.GLOW_INK_SAC) ||
                            player.getOffhandItem().is(net.minecraft.world.item.Items.INK_SAC);

                    if (held.getItem() instanceof com.boran.signbuilder.item.PaintBrushItem ||
                            held.getItem() instanceof com.boran.signbuilder.item.WrenchItem ||
                            held.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem ||
                            held.is(net.minecraft.world.item.Items.GLOW_INK_SAC) ||
                            held.is(net.minecraft.world.item.Items.INK_SAC) ||
                            holdingInk) {
                        return EventResult.pass();
                    }

                    if (player.isShiftKeyDown()) {
                        com.boran.signbuilder.client.grid.GridRaytrace.GridRay ray = com.boran.signbuilder.client.grid.GridRaytrace.getRay(hit);
                        if (ray != null && client.level.getBlockEntity(ray.pos()) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                            if (hand == InteractionHand.MAIN_HAND) {
                                com.boran.signbuilder.block.entity.GridSignBlockEntity.CellData cell = gbe.getCellAt((float) ray.u(), (float) ray.v(), ray.isBack());
                                if (cell != null) {
                                    int cellKey = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(cell.isBack, cell.gridSize, cell.col, cell.row);
                                    gbe.getCells().remove(cellKey);
                                    gbe.markRenderDirty();
                                    com.boran.signbuilder.network.ModMessages.sendToServer(
                                            new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), cellKey)
                                    );
                                    player.swing(hand);
                                    client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                    return EventResult.interruptFalse();
                                } else if (ray.isBack() && !gbe.getBackGlyph().isEmpty()) {
                                    gbe.setBackGlyph(net.minecraft.world.item.ItemStack.EMPTY);
                                    gbe.markRenderDirty();
                                    com.boran.signbuilder.network.ModMessages.sendToServer(
                                            new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), -1)
                                    );
                                    player.swing(hand);
                                    client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                    return EventResult.interruptFalse();
                                } else if (!ray.isBack() && !gbe.getFrontGlyph().isEmpty()) {
                                    gbe.setFrontGlyph(net.minecraft.world.item.ItemStack.EMPTY);
                                    gbe.markRenderDirty();
                                    com.boran.signbuilder.network.ModMessages.sendToServer(
                                            new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), -2)
                                    );
                                    player.swing(hand);
                                    client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                    return EventResult.interruptFalse();
                                }
                            }
                            return EventResult.pass();
                        }
                    }

                    if (GridModeManager.isGridModeActive() && GridModeManager.isHoldingLetterBlock(player)) {
                        int gridSize = GridModeManager.getCurrentGridSize();
                        GridRaytrace.GridHit gridHit = GridRaytrace.getGridHit(hit, gridSize);
                        if (gridHit != null) {
                            net.minecraft.core.BlockPos targetPos = gridHit.pos();
                            if (client.level.getBlockState(targetPos).getBlock() instanceof LetterBlock) {
                                return EventResult.interruptFalse();
                            }
                            boolean isBack = com.boran.signbuilder.block.GridSignBlock.isBackIndex(gridHit.index(), gridSize);
                            int rawIndex = com.boran.signbuilder.block.GridSignBlock.getLocalIndex(gridHit.index(), gridSize);
                            int col = rawIndex % gridSize;
                            int row = rawIndex / gridSize;

                            if (client.level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                                if (gbe.hasCollision(gridSize, col, row, isBack)) {
                                    client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F));
                                    return EventResult.interruptFalse();
                                }
                            }

                            ItemStack item = player.getItemInHand(hand);
                            if (item.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
                                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(letterBlock);
                                String blockPath = blockId.getPath();
                                ModMessages.sendToServer(
                                        new PlaceGridLetterC2SPacket(
                                                gridHit.pos(), gridHit.face(), player.getDirection(), gridHit.index(), gridSize, blockPath
                                        )
                                );
                                player.swing(hand);
                                return EventResult.interruptFalse();
                            }
                        }
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(client -> {
            KeyMappingRegistry.register(GridModeManager.TOGGLE_GRID_KEY);
            MenuRegistry.registerScreenFactory(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);

            BlockEntityRendererRegistry.register(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get(), GridSignBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.LCD_SCREEN_BLOCK_ENTITY.get(), com.boran.signbuilder.client.render.LcdScreenBlockEntityRenderer::new);

            ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
                if (level != null && pos != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof LetterBlockEntity letter) {
                        if (state.is(ModBlocks.BACKPLATE.get())) {
                            return tintIndex == 1 ? letter.getBackplateBackColor() : letter.getBackplateFrontColor();
                        }
                        if (tintIndex == 0) {
                            return letter.getRgbColor();
                        }
                        if (tintIndex == 1) {
                            return letter.getBackplateBackColor();
                        }
                    }
                }
                return 0xFFFFFF;
            }, getLetterBlocksAndBackplate());
        });
    }

    private static Block[] getLetterBlocksAndBackplate() {
        List<Block> blocks = new ArrayList<>();
        for (var blockSupplier : ModBlocks.BLOCKS) {
            Block block = blockSupplier.get();
            if (block instanceof LetterBlock || block == ModBlocks.BACKPLATE.get()) {
                blocks.add(block);
            }
        }
        return blocks.toArray(new Block[0]);
    }
    public static boolean onMouseScrolled(double amountX, double amountY) {
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client.player == null || client.level == null) return false;
        boolean shiftDown = net.minecraft.client.gui.screens.Screen.hasShiftDown()
                || client.options.keyShift.isDown()
                || client.player.isShiftKeyDown();
        if (!shiftDown) return false;
        double delta = (Math.abs(amountY) > 0.01) ? amountY : amountX;
        if (Math.abs(delta) < 0.01) return false;

        net.minecraft.world.item.ItemStack main = client.player.getMainHandItem();
        net.minecraft.world.item.ItemStack off = client.player.getOffhandItem();
        net.minecraft.world.item.ItemStack stack = (main.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) ? main
                : ((off.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) ? off : net.minecraft.world.item.ItemStack.EMPTY);
        if (stack.isEmpty()) return false;

        net.minecraft.nbt.CompoundTag tag = stack.getOrCreateTag();
        int currentDir = tag.getInt("PlacementDirection");
        int nextDir = (delta > 0) ? ((currentDir + 1) % 3) : ((currentDir + 2) % 3);

        tag.putInt("PlacementDirection", nextDir);
        com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.BlueprintDirectionC2SPacket(nextDir));

        client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));

        net.minecraft.network.chat.Component dirComp = switch (nextDir) {
            case 1 -> net.minecraft.network.chat.Component.translatable("gui.signbuilder.blueprint.dir.forward").withStyle(net.minecraft.ChatFormatting.AQUA);
            case 2 -> net.minecraft.network.chat.Component.translatable("gui.signbuilder.blueprint.dir.right_to_left").withStyle(net.minecraft.ChatFormatting.YELLOW);
            default -> net.minecraft.network.chat.Component.translatable("gui.signbuilder.blueprint.dir.left_to_right").withStyle(net.minecraft.ChatFormatting.GREEN);
        };
        client.player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.signbuilder.blueprint.direction_changed", dirComp).withStyle(net.minecraft.ChatFormatting.GOLD),
                true
        );
        return true;
    }
}
