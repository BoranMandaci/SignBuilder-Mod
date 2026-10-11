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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record PlaceGridLetterC2SPacket(BlockPos pos, Direction face, Direction playerFacing, int index, int gridSize, String character) {

    public PlaceGridLetterC2SPacket(FriendlyByteBuf buf) {
        this(
                buf.readBlockPos(),
                buf.readEnum(Direction.class),
                buf.readEnum(Direction.class),
                buf.readInt(),
                buf.readInt(),
                buf.readUtf()
        );
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnum(this.face);
        buf.writeEnum(this.playerFacing);
        buf.writeInt(this.index);
        buf.writeInt(this.gridSize);
        buf.writeUtf(this.character);
    }

    public void handle(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        Level level = player.level();
        if (level == null || !level.isLoaded(pos)) return;

        com.boran.signbuilder.server.ServerGridModeState.setGridSize(player.getUUID(), this.gridSize);

        BlockState targetState = level.getBlockState(pos);
        BlockPos targetPos = pos;

        int targetIndex = this.index;

        boolean serverIsBack = false;
        AttachFace aFace = targetState.hasProperty(BlockStateProperties.ATTACH_FACE)
                ? targetState.getValue(BlockStateProperties.ATTACH_FACE)
                : AttachFace.WALL;

        if (aFace != AttachFace.WALL) {
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
            if (frontDirection != null && face == frontDirection) {
                serverIsBack = false;
            } else if (frontDirection != null && face == frontDirection.getOpposite()) {
                serverIsBack = true;
            } else if (player != null) {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double dot = look.x * com.boran.signbuilder.block.SignRotation.facingX(signNormalRotation)
                        + look.z * com.boran.signbuilder.block.SignRotation.facingZ(signNormalRotation);
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

            if (serverIsBack && targetIndex < 16) {
                targetIndex += 16;
            } else if (!serverIsBack && targetIndex >= 16) {
                targetIndex -= 16;
            }
        } else {
            if (targetIndex >= 16) {
                targetIndex -= 16;
            }
        }

        if (!(targetState.getBlock() instanceof GridSignBlock)) {
            boolean isBackplate = targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
            if (!isBackplate && targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    isBackplate = lbe.getEffectiveMaster().hasBackplate();
                }
            }
            boolean hasBackplateProperties = false;
            com.boran.signbuilder.block.SignMaterial bpFMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            com.boran.signbuilder.block.SignMaterial bpBMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            int bpFColor = 0xFFFFFF;
            int bpBColor = 0xFFFFFF;
            boolean bpFRainbow = false;
            boolean bpBRainbow = false;
            int bpFacingRotation = 0;
            int bpWrenchMode = 0;
            boolean bpActive = false;
            boolean bpDetectsMonsters = true;
            boolean bpDetectsAnimals = false;
            int bpOnTicks = 10, bpOffTicks = 10, bpType = 0, bpRange = 8, bpOffRange = 8, bpCloseDelay = 0;
            boolean bpNightOnly = true, bpPlayers = true, bpLowPower = false, bpLookOnly = true;
            ItemStack bpBackGlyph = ItemStack.EMPTY;
            ItemStack oldFrontGlyph = ItemStack.EMPTY;
            BlockState previousState = targetState;

            if (isBackplate) {
                targetPos = pos;
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe.getEffectiveMaster();
                    hasBackplateProperties = effective.hasBackplate() || targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
                    if (hasBackplateProperties) {
                        bpBackGlyph = effective.getBackGlyph();
                        if (previousState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
                            oldFrontGlyph = effective.getDroppedItemStack(previousState);
                            CompoundTag itemTag = oldFrontGlyph.getTag();
                            if (itemTag != null && itemTag.contains("BlockEntityTag", 10)) {
                                CompoundTag beTag = itemTag.getCompound("BlockEntityTag");
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
                                if (beTag.isEmpty()) itemTag.remove("BlockEntityTag");
                            }
                        }
                        bpFMat = effective.getBackplateFrontMaterial();
                        bpBMat = effective.getBackplateBackMaterial();
                        bpFColor = effective.getBackplateFrontColor();
                        bpBColor = effective.getBackplateBackColor();
                        bpFRainbow = effective.isBackplateFrontRainbow();
                        bpBRainbow = effective.isBackplateBackRainbow();
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

            if (targetIndex >= 16) {
                if (bpBackGlyph != null && !bpBackGlyph.isEmpty()) {
                    if (!player.isCreative()) {
                        net.minecraft.world.level.block.Block.popResource(level, targetPos, bpBackGlyph.copy());
                    }
                    bpBackGlyph = ItemStack.EMPTY;
                }
            } else {
                if (!oldFrontGlyph.isEmpty()) {
                    if (!player.isCreative()) {
                        net.minecraft.world.level.block.Block.popResource(level, targetPos, oldFrontGlyph.copy());
                    }
                    oldFrontGlyph = ItemStack.EMPTY;
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
                gridBe.setFacingRotation(bpFacingRotation);
                gridBe.setLightConfiguration(bpWrenchMode, bpActive, bpDetectsMonsters, bpDetectsAnimals, bpOnTicks, bpOffTicks, bpType, bpRange, bpOffRange, bpCloseDelay, bpNightOnly, bpPlayers, bpLowPower, bpLookOnly);
                if (!oldFrontGlyph.isEmpty()) {
                    gridBe.setFrontGlyph(oldFrontGlyph);
                }
                if (bpBackGlyph != null && !bpBackGlyph.isEmpty()) {
                    gridBe.setBackGlyph(bpBackGlyph);
                }
                GridSignBlock.updateLightLevel(level, targetPos, newState, gridBe);
            }
        }

        if (level.getBlockEntity(targetPos) instanceof GridSignBlockEntity gridBe) {
            if (gridBe.getGridSize() != gridSize) {
                gridBe.setGridSize(gridSize);
            }
            if (targetIndex >= 16 && !gridBe.getBackGlyph().isEmpty()) {
                if (!player.isCreative()) {
                    net.minecraft.world.level.block.Block.popResource(level, targetPos, gridBe.getBackGlyph().copy());
                }
                gridBe.setBackGlyph(ItemStack.EMPTY);
            }
            if (targetIndex < 16 && !gridBe.getFrontGlyph().isEmpty()) {
                if (!player.isCreative()) {
                    net.minecraft.world.level.block.Block.popResource(level, targetPos, gridBe.getFrontGlyph().copy());
                }
                gridBe.setFrontGlyph(ItemStack.EMPTY);
            }
            gridBe.setCell(targetIndex, character, SignMaterial.DEFAULT, 0xFFFFFF, false, 0);

            if (!player.isCreative()) {
                ItemStack mainHand = player.getMainHandItem();
                boolean found = false;
                if (mainHand.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LetterBlock) {
                    mainHand.shrink(1);
                    found = true;
                }
                if (!found) {
                    ItemStack offHand = player.getOffhandItem();
                    if (offHand.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LetterBlock) {
                        offHand.shrink(1);
                    }
                }
            }

            level.playSound(null, targetPos, targetState.getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
        }
    }
}
