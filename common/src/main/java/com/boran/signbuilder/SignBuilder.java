package com.boran.signbuilder;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.item.ModCreativeModeTabs;
import com.boran.signbuilder.item.ModItems;
import com.boran.signbuilder.item.PaintBrushItem;
import com.boran.signbuilder.menu.ModMenuTypes;
import com.boran.signbuilder.network.ModMessages;
import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SignBuilder {

    public static final String MODID = "signbuilder";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ModBlocks.register();
        ModItems.register();

        ModBlockEntities.register();
        ModMenuTypes.register();
        ModCreativeModeTabs.register();

        ModMessages.register();

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
                ItemStack held = player.getItemInHand(hand);
                if (held.getItem() instanceof PaintBrushItem) {
                    return EventResult.pass();
                }

                Level level = player.level();
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof LetterBlock) {
                    if (LetterBlock.tryDetachBackplate(level, pos, player)) {
                        return EventResult.interruptTrue();
                    }
                }
            }
            return EventResult.pass();
        });

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            com.boran.signbuilder.client.ClientModEvents.init();
        });
    }

    public static int getColorHex(int index) {
        return switch (index) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8;
            case 4 -> 0xE5E533; case 5 -> 0x7FCC19; case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C;
            case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0x993333; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }
}