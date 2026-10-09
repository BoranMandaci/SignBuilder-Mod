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
import net.minecraft.world.level.Level;
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

        if (!(targetState.getBlock() instanceof GridSignBlock)) {
            boolean isBackplate = targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
            if (!isBackplate && targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe;
                    if (lbe.isDummy()) {
                        net.minecraft.world.level.block.entity.BlockEntity me = level.getBlockEntity(lbe.getMasterPos());
                        if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) {
                            effective = masterBe;
                        }
                    }
                    isBackplate = effective.hasBackplate();
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

            if (isBackplate) {
                targetPos = pos;
                if (level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe;
                    if (lbe.isDummy()) {
                        net.minecraft.world.level.block.entity.BlockEntity me = level.getBlockEntity(lbe.getMasterPos());
                        if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) {
                            effective = masterBe;
                        }
                    }
                    hasBackplateProperties = effective.hasBackplate() || targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
                    if (hasBackplateProperties) {
                        bpFMat = effective.getBackplateFrontMaterial();
                        bpBMat = effective.getBackplateBackMaterial();
                        bpFColor = effective.getBackplateFrontColor();
                        bpBColor = effective.getBackplateBackColor();
                        bpFRainbow = effective.isBackplateFrontRainbow();
                        bpBRainbow = effective.isBackplateBackRainbow();
                        bpFacingRotation = effective.getFacingRotation();
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
            }

            BlockState newState = ModBlocks.GRID_SIGN.get().defaultBlockState()
                    .setValue(GridSignBlock.FACE, attachFace)
                    .setValue(GridSignBlock.FACING, blockFacing);

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
                GridSignBlock.updateLightLevel(level, targetPos, newState, gridBe);
            }
        }

        if (level.getBlockEntity(targetPos) instanceof GridSignBlockEntity gridBe) {
            if (gridBe.getGridSize() != gridSize) {
                gridBe.setGridSize(gridSize);
            }
            gridBe.setCell(index, character, SignMaterial.DEFAULT, 0xFFFFFF, false, 0);

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
