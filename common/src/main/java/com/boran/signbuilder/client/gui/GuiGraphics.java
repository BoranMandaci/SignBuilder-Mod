package com.boran.signbuilder.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiGraphics extends GuiComponent {
    private final PoseStack poseStack;
    private static Runnable deferredTooltip = null;

    public GuiGraphics(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public PoseStack pose() {
        return poseStack;
    }

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        GuiComponent.fill(poseStack, minX, minY, maxX, maxY, color);
    }

    public void fill(int minX, int minY, int maxX, int maxY, int z, int color) {
        poseStack.pushPose();
        poseStack.translate(0, 0, z);
        GuiComponent.fill(poseStack, minX, minY, maxX, maxY, color);
        poseStack.popPose();
    }

    public void fillGradient(int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        super.fillGradient(poseStack, minX, minY, maxX, maxY, colorFrom, colorTo);
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean dropShadow) {
        if (text == null) return 0;
        return dropShadow ? font.drawShadow(poseStack, text, (float) x, (float) y, color) : font.draw(poseStack, text, (float) x, (float) y, color);
    }

    public int drawString(Font font, String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean dropShadow) {
        if (text == null) return 0;
        return dropShadow ? font.drawShadow(poseStack, text, (float) x, (float) y, color) : font.draw(poseStack, text, (float) x, (float) y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        if (text == null) return 0;
        return dropShadow ? font.drawShadow(poseStack, text, (float) x, (float) y, color) : font.draw(poseStack, text, (float) x, (float) y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        GuiComponent.drawCenteredString(poseStack, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        GuiComponent.drawCenteredString(poseStack, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        GuiComponent.drawCenteredString(poseStack, font, text, x, y, color);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        GuiComponent.blit(poseStack, x, y, 0, (float) uOffset, (float) vOffset, uWidth, vHeight, 256, 256);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        GuiComponent.blit(poseStack, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
    }

    public void renderTooltip(Font font, ItemStack stack, int mouseX, int mouseY) {
        PoseStack pose = this.poseStack;
        deferredTooltip = () -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.renderTooltip(pose, screen.getTooltipFromItem(stack), stack.getTooltipImage(), mouseX, mouseY);
            }
        };
    }

    public void renderTooltip(Font font, Component text, int mouseX, int mouseY) {
        PoseStack pose = this.poseStack;
        deferredTooltip = () -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.renderTooltip(pose, text, mouseX, mouseY);
            }
        };
    }

    public void renderComponentTooltip(Font font, List<Component> text, int mouseX, int mouseY) {
        PoseStack pose = this.poseStack;
        deferredTooltip = () -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.renderComponentTooltip(pose, text, mouseX, mouseY);
            }
        };
    }

    public void renderTooltip(Font font, List<? extends FormattedCharSequence> text, int mouseX, int mouseY) {
        PoseStack pose = this.poseStack;
        deferredTooltip = () -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.renderTooltip(pose, text, mouseX, mouseY);
            }
        };
    }

    public static void flushTooltip() {
        if (deferredTooltip != null) {
            Runnable action = deferredTooltip;
            deferredTooltip = null;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            action.run();
        }
    }
}
