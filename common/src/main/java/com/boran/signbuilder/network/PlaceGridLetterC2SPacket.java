package com.boran.signbuilder.network;

import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record PlaceGridLetterC2SPacket(BlockPos pos, Direction face, Direction playerFacing, int index, int gridSize, String character) implements CustomPacketPayload {
    public static final Type<PlaceGridLetterC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("signbuilder", "place_grid_letter"));

    public static final StreamCodec<FriendlyByteBuf, PlaceGridLetterC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                buf.writeEnum(packet.face());
                buf.writeEnum(packet.playerFacing());
                buf.writeInt(packet.index());
                buf.writeInt(packet.gridSize());
                buf.writeUtf(packet.character());
            },
            buf -> new PlaceGridLetterC2SPacket(
                    buf.readBlockPos(),
                    buf.readEnum(Direction.class),
                    buf.readEnum(Direction.class),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        Level level = player.level();
        if (level == null || !level.isLoaded(pos)) return;

        BlockState targetState = level.getBlockState(pos);
        BlockPos targetPos = pos;
        int targetIndex = this.index;
        boolean serverIsBack = false;

        AttachFace aFace = targetState.hasProperty(BlockStateProperties.ATTACH_FACE)
                ? targetState.getValue(BlockStateProperties.ATTACH_FACE)
                : AttachFace.WALL;

        if (aFace == AttachFace.WALL) {
            Direction blockFacing = targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    ? targetState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                    : face;
            if (face == blockFacing.getOpposite()) {
                serverIsBack = true;
            } else if (face == blockFacing) {
                serverIsBack = false;
            } else if (player != null) {
                int rot = com.boran.signbuilder.block.SignRotation.fromDirection(blockFacing);
                double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rot)
                        + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rot);
                serverIsBack = side < 0.0;
            }
        } else {
            int signNormalRotation = 0;
            if (targetState.getBlock() instanceof GridSignBlock && level.getBlockEntity(pos) instanceof GridSignBlockEntity gbe) {
                signNormalRotation = gbe.getFacingRotation();
            } else if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe.getEffectiveMaster();
                int rot = effective.getFacingRotation();
                if (targetState.getBlock() instanceof LetterBlock) {
                    signNormalRotation = Math.floorMod(rot - 2, 8);
                } else {
                    signNormalRotation = rot;
                }
            } else if (targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                signNormalRotation = com.boran.signbuilder.block.SignRotation.fromDirection(targetState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
            Direction frontDirection = com.boran.signbuilder.block.SignRotation.cardinalDirection(signNormalRotation);

            if (frontDirection != null && face == frontDirection.getOpposite()) {
                serverIsBack = true;
            } else if (frontDirection != null && face == frontDirection) {
                serverIsBack = false;
            } else if (player != null) {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double dot = look.x * com.boran.signbuilder.block.SignRotation.facingX(signNormalRotation) + look.z * com.boran.signbuilder.block.SignRotation.facingZ(signNormalRotation);
                if (dot > 0.05) {
                    serverIsBack = true;
                } else if (dot < -0.05) {
                    serverIsBack = false;
                } else {
                    double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(signNormalRotation)
                            + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(signNormalRotation);
                    serverIsBack = side < 0.0;
                }
            }
        }

        int backOffset = GridSignBlock.getBackOffset(this.gridSize);
        if (serverIsBack && !GridSignBlock.isBackIndex(targetIndex, this.gridSize)) {
            targetIndex += backOffset;
        } else if (!serverIsBack && GridSignBlock.isBackIndex(targetIndex, this.gridSize)) {
            targetIndex = GridSignBlock.getLocalIndex(targetIndex, this.gridSize);
        }

        if (!(targetState.getBlock() instanceof GridSignBlock)) {
            boolean isBackplate = targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
            if (!isBackplate && targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe.getEffectiveMaster();
                    isBackplate = effective.hasBackplate() && effective.getSize() == 1;
                }
            }
            boolean hasBackplateProperties = false;
            com.boran.signbuilder.block.SignMaterial bpFMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            com.boran.signbuilder.block.SignMaterial bpBMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            int bpFColor = 0xFFFFFF;
            int bpBColor = 0xFFFFFF;
            boolean bpFRainbow = false;
            boolean bpBRainbow = false;
            boolean bpFGlow = false;
            boolean bpBGlow = false;
            boolean bpFrontGlyphGlow = false;
            boolean bpBackGlyphGlow = false;
            int bpFacingRotation = 0;
            int bpWrenchMode = 0;
            boolean bpActive = false;
            boolean bpDetectsMonsters = true;
            boolean bpDetectsAnimals = false;
            int bpOnTicks = 10, bpOffTicks = 10, bpType = 0, bpRange = 8, bpOffRange = 8, bpCloseDelay = 0;
            boolean bpNightOnly = true, bpPlayers = true, bpLowPower = false, bpLookOnly = true;
            ItemStack bpBackGlyph = ItemStack.EMPTY;
            ItemStack oldFrontGlyph = ItemStack.EMPTY;

            if (isBackplate) {
                targetPos = pos;
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe.getEffectiveMaster();
                    hasBackplateProperties = effective.hasBackplate() || targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
                    if (hasBackplateProperties) {
                        bpBackGlyph = effective.getBackGlyph();
                        bpFrontGlyphGlow = effective.isGlowInked();
                        bpBackGlyphGlow = effective.isBackGlyphGlowInked();
                        if (targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
                            oldFrontGlyph = effective.getDroppedItemStack(targetState);
                            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, oldFrontGlyph, beTag -> {
                                beTag.remove("BackGlyph");
                                beTag.remove("HasBackplate");
                                beTag.remove("BPFrontMat");
                                beTag.remove("BPBackMat");
                                beTag.remove("BPFrontColor");
                                beTag.remove("BPBackColor");
                                beTag.remove("BPFrontRainbow");
                                beTag.remove("BPBackRainbow");
                                beTag.remove("BackplateFrontMaterial");
                                beTag.remove("BackplateBackMaterial");
                                beTag.remove("BackplateFrontColor");
                                beTag.remove("BackplateBackColor");
                                beTag.remove("BackplateFrontRainbow");
                                beTag.remove("BackplateBackRainbow");
                                beTag.remove("BPFrontGlowInked");
                                beTag.remove("BPBackGlowInked");
                                beTag.remove("BackplateFrontGlowInked");
                                beTag.remove("BackplateBackGlowInked");
                            });
                            net.minecraft.world.item.component.CustomData cd = oldFrontGlyph.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
                            if (cd != null && cd.isEmpty()) {
                                oldFrontGlyph.remove(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
                            }
                        }
                        bpFMat = effective.getBackplateFrontMaterial();
                        if (bpFMat == com.boran.signbuilder.block.SignMaterial.DEFAULT && targetState.hasProperty(com.boran.signbuilder.block.BackplateBlock.MATERIAL)) {
                            bpFMat = targetState.getValue(com.boran.signbuilder.block.BackplateBlock.MATERIAL);
                        }
                        bpBMat = effective.getBackplateBackMaterial();
                        bpFColor = effective.getBackplateFrontColor();
                        bpBColor = effective.getBackplateBackColor();
                        bpFRainbow = effective.isBackplateFrontRainbow();
                        bpBRainbow = effective.isBackplateBackRainbow();
                        bpFGlow = effective.isBackplateFrontGlowInked();
                        bpBGlow = effective.isBackplateBackGlowInked();
                        bpFacingRotation = effective.getFacingRotation();
                        if (targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock && targetState.hasProperty(BlockStateProperties.ATTACH_FACE) && targetState.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL) {
                            bpFacingRotation = Math.floorMod(bpFacingRotation - 2, 8);
                        }
                        bpWrenchMode = effective.getWrenchMode();
                        bpActive = effective.isActive();
                        bpDetectsMonsters = effective.doesDetectMonsters();
                        bpDetectsAnimals = effective.doesDetectAnimals();
                        bpOnTicks = effective.getCustomLightOnTicks();
                        bpOffTicks = effective.getCustomLightOffTicks();
                        bpType = effective.getCustomLightType();
                        bpRange = effective.getCustomLightRange();
                        bpOffRange = effective.getCustomLightOffRange();
                        bpCloseDelay = effective.getCustomLightCloseDelayTicks();
                        bpNightOnly = effective.isCustomLightNightOnly();
                        bpPlayers = effective.doesCustomLightDetectPlayers();
                        bpLowPower = effective.isCustomLightLowPower();
                        bpLookOnly = effective.isCustomLightLookOnly();
                    }
                } else if (targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock) {
                    hasBackplateProperties = true;
                    if (targetState.hasProperty(com.boran.signbuilder.block.BackplateBlock.MATERIAL)) {
                        bpFMat = targetState.getValue(com.boran.signbuilder.block.BackplateBlock.MATERIAL);
                    }
                    bpFacingRotation = com.boran.signbuilder.block.SignRotation.fromDirection(targetState.getValue(BlockStateProperties.HORIZONTAL_FACING));
                }
            } else {
                if (face == Direction.UP || face == Direction.DOWN) return;
                targetPos = pos.relative(face);
                if (!level.getBlockState(targetPos).canBeReplaced()) return;
            }

            AttachFace attachFace = AttachFace.WALL;
            if (isBackplate && targetState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                attachFace = targetState.getValue(BlockStateProperties.ATTACH_FACE);
            }

            Direction blockFacing = face;
            if (isBackplate && targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                blockFacing = targetState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock && attachFace != AttachFace.WALL) {
                    blockFacing = blockFacing.getCounterClockWise();
                }
            }

            BlockState newState = ModBlocks.GRID_SIGN.get().defaultBlockState()
                    .setValue(GridSignBlock.FACE, attachFace)
                    .setValue(GridSignBlock.FACING, blockFacing);

            if (GridSignBlock.isBackIndex(targetIndex, this.gridSize)) {
                if (bpBackGlyph != null && !bpBackGlyph.isEmpty()) {
                    if (!player.isCreative()) {
                        net.minecraft.world.level.block.Block.popResource(level, targetPos, bpBackGlyph.copy());
                        if (bpBackGlyphGlow) {
                            net.minecraft.world.level.block.Block.popResource(level, targetPos, new ItemStack(Items.GLOW_INK_SAC));
                        }
                    }
                    bpBackGlyph = ItemStack.EMPTY;
                    bpBackGlyphGlow = false;
                }
            } else {
                if (!oldFrontGlyph.isEmpty()) {
                    if (!player.isCreative()) {
                        net.minecraft.world.level.block.Block.popResource(level, targetPos, oldFrontGlyph.copy());
                        if (bpFrontGlyphGlow) {
                            net.minecraft.world.level.block.Block.popResource(level, targetPos, new ItemStack(Items.GLOW_INK_SAC));
                        }
                    }
                    oldFrontGlyph = ItemStack.EMPTY;
                    bpFrontGlyphGlow = false;
                }
            }

            level.setBlock(targetPos, newState, 3);
            targetState = newState;

            if (hasBackplateProperties && level.getBlockEntity(targetPos) instanceof GridSignBlockEntity gridBe) {
                gridBe.setHasBackplate(true);
                gridBe.setBackplateFrontMaterial(bpFMat);
                gridBe.setBackplateBackMaterial(bpBMat);
                gridBe.setBackplateFrontColor(bpFColor);
                gridBe.setBackplateBackColor(bpBColor);
                gridBe.setBackplateFrontRainbow(bpFRainbow);
                gridBe.setBackplateBackRainbow(bpBRainbow);
                gridBe.setBackplateFrontGlowInked(bpFGlow);
                gridBe.setBackplateBackGlowInked(bpBGlow);
                gridBe.setFacingRotation(bpFacingRotation);
                gridBe.setLightConfiguration(bpWrenchMode, bpActive, bpDetectsMonsters, bpDetectsAnimals, bpOnTicks, bpOffTicks, bpType, bpRange, bpOffRange, bpCloseDelay, bpNightOnly, bpPlayers, bpLowPower, bpLookOnly);
                if (!oldFrontGlyph.isEmpty()) {
                    gridBe.setFrontGlyph(oldFrontGlyph);
                    gridBe.setFrontGlyphGlowInked(bpFrontGlyphGlow);
                }
                if (bpBackGlyph != null && !bpBackGlyph.isEmpty()) {
                    gridBe.setBackGlyph(bpBackGlyph);
                    gridBe.setBackGlyphGlowInked(bpBackGlyphGlow);
                }
                gridBe.markRenderDirty();
                gridBe.setChanged();
                gridBe.sync();
                level.sendBlockUpdated(targetPos, newState, newState, 3);
                GridSignBlock.updateLightLevel(level, targetPos, newState, gridBe);
            }
        }

        if (level.getBlockEntity(targetPos) instanceof GridSignBlockEntity gridBe) {
            boolean isBack = com.boran.signbuilder.block.GridSignBlock.isBackIndex(targetIndex, gridSize);
            int rawIndex = com.boran.signbuilder.block.GridSignBlock.getLocalIndex(targetIndex, gridSize);
            int col = rawIndex % gridSize;
            int row = rawIndex / gridSize;

            if (gridBe.hasCollision(gridSize, col, row, isBack)) {
                return;
            }

            if (gridBe.getCells().isEmpty()) {
                gridBe.setGridSize(gridSize);
            }
            if (isBack && !gridBe.getBackGlyph().isEmpty()) {
                if (!player.isCreative()) {
                    net.minecraft.world.level.block.Block.popResource(level, targetPos, gridBe.getBackGlyph().copy());
                    if (gridBe.isBackGlyphGlowInked()) {
                        net.minecraft.world.level.block.Block.popResource(level, targetPos, new ItemStack(Items.GLOW_INK_SAC));
                    }
                }
                gridBe.setBackGlyph(ItemStack.EMPTY);
                gridBe.setBackGlyphGlowInked(false);
            }
            if (!isBack && !gridBe.getFrontGlyph().isEmpty()) {
                if (!player.isCreative()) {
                    net.minecraft.world.level.block.Block.popResource(level, targetPos, gridBe.getFrontGlyph().copy());
                    if (gridBe.isFrontGlyphGlowInked()) {
                        Block.popResource(level, targetPos, new ItemStack(Items.GLOW_INK_SAC));
                    }
                }
                gridBe.setFrontGlyph(ItemStack.EMPTY);
                gridBe.setFrontGlyphGlowInked(false);
            }

            ItemStack mainHand = player.getMainHandItem();
            ItemStack usedStack = ItemStack.EMPTY;
            if (mainHand.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LetterBlock) {
                usedStack = mainHand;
            } else {
                ItemStack offHand = player.getOffhandItem();
                if (offHand.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LetterBlock) {
                    usedStack = offHand;
                }
            }
            net.minecraft.nbt.CompoundTag beTag = usedStack.getOrDefault(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            boolean itemGlowInked = beTag != null && beTag.getBoolean("IsGlowInked");

            gridBe.setCell(col, row, gridSize, isBack, character, SignMaterial.DEFAULT, 0xFFFFFF, false, 0, itemGlowInked);

            if (!player.isCreative() && !usedStack.isEmpty()) {
                usedStack.shrink(1);
            }

            level.playSound(null, targetPos, targetState.getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
        }
    }
}
