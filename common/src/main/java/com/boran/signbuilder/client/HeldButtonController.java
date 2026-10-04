package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.item.PaintBrushItem;
import com.boran.signbuilder.item.SignBlueprintItem;
import com.boran.signbuilder.item.WrenchItem;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.WrenchHoldC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class HeldButtonController {
    private static final int KEEPALIVE_INTERVAL_TICKS = 20;
    private static BlockPos heldPos;
    private static int refreshTicks;

    private HeldButtonController() {
    }

    public static void tick(Minecraft minecraft) {
        BlockPos targetPos = null;
        var mainHandItem = minecraft.player == null ? null : minecraft.player.getMainHandItem().getItem();
        var offHandItem = minecraft.player == null ? null : minecraft.player.getOffhandItem().getItem();
        boolean usingSignTool = isSignTool(mainHandItem) || isSignTool(offHandItem);
        boolean useHeld = minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !usingSignTool
                && !minecraft.player.isShiftKeyDown()
                && minecraft.options.keyUse.isDown();

        if (useHeld && minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            var state = minecraft.level.getBlockState(hit.getBlockPos());
            if (state.getBlock() instanceof LetterBlock && state.hasProperty(LetterBlock.FACE)
                    && state.getValue(LetterBlock.FACE) == AttachFace.WALL
                    && minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof LetterBlockEntity rawLetter) {
                LetterBlockEntity master = rawLetter.getEffectiveMaster();
                if (master.getButtonMode() == 4) targetPos = master.getBlockPos().immutable();
            }
        }

        if (heldPos != null && (!useHeld || targetPos == null || !heldPos.equals(targetPos))) {
            ModMessages.sendToServer(new WrenchHoldC2SPacket(heldPos, false));
            heldPos = null;
            refreshTicks = 0;
        }

        if (targetPos == null) return;
        if (heldPos == null) {
            heldPos = targetPos;
            refreshTicks = 0;
            ModMessages.sendToServer(new WrenchHoldC2SPacket(heldPos, true));
        } else if (++refreshTicks >= KEEPALIVE_INTERVAL_TICKS) {
            refreshTicks = 0;
            ModMessages.sendToServer(new WrenchHoldC2SPacket(heldPos, true));
        }
    }

    private static boolean isSignTool(Object item) {
        return item instanceof WrenchItem || item instanceof PaintBrushItem || item instanceof SignBlueprintItem;
    }
}
