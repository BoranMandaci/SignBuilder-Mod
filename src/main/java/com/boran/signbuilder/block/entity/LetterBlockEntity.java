package com.boran.signbuilder.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LetterBlockEntity extends BlockEntity {
    private int rgbColor = 0xFFFFFF;

    public LetterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // Blok yeni koyulduğunda veya Neon açıldığında eski rengini hemen yükler
        if (state.hasProperty(com.boran.signbuilder.block.ModBlocks.COLOR)) {
            this.rgbColor = getActualHexColor(state.getValue(com.boran.signbuilder.block.ModBlocks.COLOR));
        }
    }

    public void setRgbColor(int color) {
        this.rgbColor = color;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getRgbColor() {
        return rgbColor;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RGBColor", this.rgbColor);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RGBColor")) {
            this.rgbColor = tag.getInt("RGBColor");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (level != null && level.isClientSide) {
            // Veri gelince harfi tekrar çiz (Neon siyahlaşma sorununu çözer)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static int getActualHexColor(int colorValue) {
        if (colorValue > 15) return colorValue;
        return switch (colorValue) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8;
            case 4 -> 0xE5E533; case 5 -> 0x7FCC19; case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C;
            case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0x993333; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }
}