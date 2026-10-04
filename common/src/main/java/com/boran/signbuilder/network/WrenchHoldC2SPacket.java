package com.boran.signbuilder.network;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WrenchHoldC2SPacket {
    private final BlockPos pos;
    private final boolean pressed;

    public WrenchHoldC2SPacket(BlockPos pos, boolean pressed) {
        this.pos = pos.immutable();
        this.pressed = pressed;
    }

    public WrenchHoldC2SPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.pressed = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.pressed);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return;
        if (!player.level().hasChunkAt(this.pos)) return;
        if (this.pressed) {
            if (player.distanceToSqr(Vec3.atCenterOf(this.pos)) > 100.0) return;
            HitResult hitResult = player.pick(8.0D, 1.0F, false);
            if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;
            BlockEntity hitEntity = player.level().getBlockEntity(blockHit.getBlockPos());
            if (!(hitEntity instanceof LetterBlockEntity hitLetter)
                    || !hitLetter.getEffectiveMaster().getBlockPos().equals(this.pos)) return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
        if (!(blockEntity instanceof LetterBlockEntity rawLetter)) return;
        LetterBlockEntity letter = rawLetter.getEffectiveMaster();
        if (this.pressed) {
            BlockState state = letter.getBlockState();
            if (letter.getButtonMode() != 4 || !state.hasProperty(LetterBlock.FACE) || state.getValue(LetterBlock.FACE) != AttachFace.WALL) return;
        }
        UUID playerId = player.getUUID();
        letter.setHoldPressed(playerId, this.pressed);
    }
}
