package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.item.SignBlueprintItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class BlueprintPreviewRenderer {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final BlockPos.MutableBlockPos SCRATCH_POS = new BlockPos.MutableBlockPos();
    private static final double[] OFFSET_ZERO = {0.0, 0.0};
    private static final double[] OFFSET_NEG_1_NEG_1 = {-1.0, -1.0};
    private static final double[] OFFSET_NEG_1_ZERO = {-1.0, 0.0};
    private static final double[] OFFSET_ZERO_NEG_1 = {0.0, -1.0};
    private static final double[] OFFSET_NEG_1_NEG_06875 = {-1.0, -0.6875};
    private static final double[] OFFSET_NEG_06875_ZERO = {-0.6875, 0.0};
    private static final double[] OFFSET_NEG_03125_NEG_1 = {-0.3125, -1.0};
    private static final double[] OFFSET_ZERO_NEG_03125 = {0.0, -0.3125};
    private static final double[] OFFSET_NEG_2_NEG_2 = {-2.0, -2.0};
    private static final double[] OFFSET_ZERO_NEG_2 = {0.0, -2.0};
    private static final double[] OFFSET_NEG_2_ZERO = {-2.0, 0.0};
    private static final double[] OFFSET_NEG_2_NEG_1375 = {-2.0, -1.375};
    private static final double[] OFFSET_NEG_1375_ZERO = {-1.375, 0.0};
    private static final double[] OFFSET_NEG_0625_NEG_2 = {-0.625, -2.0};
    private static final double[] OFFSET_ZERO_NEG_0625 = {0.0, -0.625};

    public static void render(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        Camera camera = mc.gameRenderer.getMainCamera();
        if (player == null || level == null || camera == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SignBlueprintItem)) {
            stack = player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof SignBlueprintItem)) return;

        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tag == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        if (tag.getBoolean("IsGridMode")) {
            renderGridPreview(poseStack, mc, player, level, camera, tag, blockHit);
            return;
        }

        if (!tag.contains("BlueprintText")) return;
        String text = tag.getString("BlueprintText");
        if (text.isEmpty()) return;

        int size = 1;
        if (tag.contains("Size")) {
            size = tag.getInt("Size");
        } else if (tag.getBoolean("Is2x2")) {
            size = 2;
        }

        boolean isVertical = tag.getBoolean("IsVertical");
        boolean withBackplate = tag.getBoolean("WithBackplate");

        Direction clickedFace = blockHit.getDirection();
        BlockPos startPos = blockHit.getBlockPos().relative(clickedFace);

        Direction playerFacing = player.getDirection();
        boolean wall = clickedFace.getAxis() != Direction.Axis.Y;
        int rotation = wall
                ? SignRotation.fromDirection(clickedFace)
                : size > 1
                    ? SignRotation.fromDirection(playerFacing.getCounterClockWise())
                    : SignRotation.fromYaw(player.getYRot(), -2);
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);

        int stepDirY = (clickedFace == Direction.UP) ? 1 : -1;

        boolean isFloor = (clickedFace.getAxis() == Direction.Axis.Y);
        AttachFace attachFace = isFloor
                ? (clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING)
                : AttachFace.WALL;

        Direction facing = isFloor
                ? playerFacing.getCounterClockWise()
                : clickedFace;

        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        int effectiveIdx = 0;
        int textLen = text.length();

        for (int i = 0; i < textLen; ) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);
            if (c == ' ' || c == 0xFE0F) continue;

            Block block = SignBlueprintItem.getBlockForChar(c);
            if (block == null) {
                effectiveIdx++;
                continue;
            }

            BlockPos basePos;
            if (!isVertical) {
                basePos = offsetRight(startPos, rightX, rightZ, effectiveIdx * size);
            } else {
                
                int baseY = (stepDirY == 1)
                        ? startPos.getY() + (effectiveIdx * size)
                        : startPos.getY() - (size - 1) - (effectiveIdx * size);
                basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
            }

            boolean canPlace = true;
            for (int dy = 0; dy < size; dy++) {
                for (int dx = 0; dx < size; dx++) {
                    SCRATCH_POS.set(basePos).move(rightX * dx, dy, rightZ * dx);
                    if (!level.getBlockState(SCRATCH_POS).canBeReplaced()) {
                        canPlace = false;
                        break;
                    }
                }
                if (!canPlace) break;
            }

            float r = canPlace ? 0.3F : 1.0F;
            float g = canPlace ? 1.0F : 0.2F;
            float b = canPlace ? 0.4F : 0.2F;
            float a = 0.65F;

            BlockState stateToPlace = block.defaultBlockState();
            if (stateToPlace.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.ATTACH_FACE, attachFace);
            }
            if (stateToPlace.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            }

            BakedModel model = mc.getBlockRenderer().getBlockModel(stateToPlace);

            poseStack.pushPose();

            double posX = basePos.getX() - camX;
            double posY = basePos.getY() - camY;
            double posZ = basePos.getZ() - camZ;

            poseStack.translate(posX, posY, posZ);

            int rotationDelta = SignRotation.deltaDegrees(rotation, facing);
            if (size == 1) {
                if (rotationDelta != 0) {
                    poseStack.translate(0.5, 0.0, 0.5);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDelta));
                    poseStack.translate(-0.5, 0.0, -0.5);
                }
            } else if (rotationDelta != 0) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDelta));
            }

            double[] offsets = getRenderOffsets(attachFace, facing, size);
            double offsetX = offsets[0];
            double offsetZ = offsets[1];
            poseStack.translate(offsetX, 0.0, offsetZ);
            if (size > 1) poseStack.scale((float) size, (float) size, (float) size);

            int normalRotation = attachFace == AttachFace.WALL ? rotation : Math.floorMod(rotation - 2, 8);
            double normalX = SignRotation.facingX(normalRotation);
            double normalZ = SignRotation.facingZ(normalRotation);
            double normalLength = Math.sqrt(normalX * normalX + normalZ * normalZ);
            if (normalLength == 0.0) normalLength = 1.0;
            normalX /= normalLength;
            normalZ /= normalLength;
            double groupCenterX = 0.5 + rightX * (size - 1) * 0.5;
            double groupCenterZ = 0.5 + rightZ * (size - 1) * 0.5;
            int modelScale = size;

            if (withBackplate) {
                Direction plateFacing = (attachFace != AttachFace.WALL) ? facing.getCounterClockWise() : facing;
                BlockState bpState = ModBlocks.BACKPLATE.get().defaultBlockState()
                        .setValue(BackplateBlock.FACING, plateFacing)
                        .setValue(BackplateBlock.FACE, attachFace);

                BakedModel bpModel = mc.getBlockRenderer().getBlockModel(bpState);
                poseStack.pushPose();
                if (attachFace != AttachFace.WALL) {
                    ModelCentering.centerPose(poseStack, bpModel, bpState, rotationDelta, size == 1, modelScale, offsetX, offsetZ, groupCenterX, groupCenterZ);
                    ModelCentering.widenDiagonalPose(poseStack, plateFacing, rotation);
                }
                renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
                poseStack.popPose();

                double plateClearance = (attachFace == AttachFace.WALL ? 1.0 : 2.0) * size / 16.0;
                if (attachFace != AttachFace.WALL) {
                    double targetX = groupCenterX + normalX * plateClearance;
                    double targetZ = groupCenterZ + normalZ * plateClearance;
                    if (LetterBlock.isLateralDot(stateToPlace)) {
                        ModelCentering.Center currentCenter = ModelCentering.transformedCenter(model, stateToPlace, rotationDelta,
                                size == 1, modelScale, offsetX, offsetZ);
                        ModelCentering.Center targetCenter = ModelCentering.centerAlongNormal(currentCenter, targetX, targetZ, normalX, normalZ);
                        targetX = targetCenter.x();
                        targetZ = targetCenter.z();
                    }
                    ModelCentering.centerPose(poseStack, model, stateToPlace, rotationDelta, size == 1, modelScale, offsetX, offsetZ, targetX, targetZ);
                } else {
                    poseStack.translate(normalX * plateClearance / modelScale, 0.0, normalZ * plateClearance / modelScale);
                }
            } else {
                if (attachFace != AttachFace.WALL) {
                    double targetX = groupCenterX;
                    double targetZ = groupCenterZ;
                    if (LetterBlock.isLateralDot(stateToPlace)) {
                        ModelCentering.Center currentCenter = ModelCentering.transformedCenter(model, stateToPlace, rotationDelta,
                                size == 1, modelScale, offsetX, offsetZ);
                        ModelCentering.Center targetCenter = ModelCentering.centerAlongNormal(currentCenter, targetX, targetZ, normalX, normalZ);
                        targetX = targetCenter.x();
                        targetZ = targetCenter.z();
                    }
                    ModelCentering.centerPose(poseStack, model, stateToPlace, rotationDelta, size == 1, modelScale, offsetX, offsetZ, targetX, targetZ);
                }
            }

            renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

            poseStack.popPose();
            effectiveIdx++;
        }

        bufferSource.endBatch(RenderType.translucent());
    }

    private static BlockPos offsetRight(BlockPos pos, int rightX, int rightZ, int distance) {
        return pos.offset(rightX * distance, 0, rightZ * distance);
    }

    private static double[] getRenderOffsets(AttachFace face, Direction facing, int size) {
        if (size == 3) {
            if (face == AttachFace.WALL) {
                return switch (facing) {
                    case NORTH -> OFFSET_NEG_2_NEG_2;
                    case SOUTH -> OFFSET_ZERO;
                    case EAST -> OFFSET_ZERO_NEG_2;
                    case WEST -> OFFSET_NEG_2_ZERO;
                    default -> OFFSET_ZERO;
                };
            }
            return switch (facing) {
                case EAST -> OFFSET_NEG_2_NEG_1375;
                case NORTH -> OFFSET_NEG_1375_ZERO;
                case SOUTH -> OFFSET_NEG_0625_NEG_2;
                case WEST -> OFFSET_ZERO_NEG_0625;
                default -> OFFSET_ZERO;
            };
        }
        if (size == 2) {
            if (face == AttachFace.WALL) {
                return switch (facing) {
                    case NORTH -> OFFSET_NEG_1_NEG_1;
                    case SOUTH -> OFFSET_ZERO;
                    case EAST -> OFFSET_ZERO_NEG_1;
                    case WEST -> OFFSET_NEG_1_ZERO;
                    default -> OFFSET_ZERO;
                };
            }
            return switch (facing) {
                case EAST -> OFFSET_NEG_1_NEG_06875;
                case NORTH -> OFFSET_NEG_06875_ZERO;
                case SOUTH -> OFFSET_NEG_03125_NEG_1;
                case WEST -> OFFSET_ZERO_NEG_03125;
                default -> OFFSET_ZERO;
            };
        }
        return OFFSET_ZERO;
    }

    private static void renderGhostModel(PoseStack poseStack, VertexConsumer consumer, BakedModel model, BlockState state, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        int red = (int) (r * 255.0F);
        int green = (int) (g * 255.0F);
        int blue = (int) (b * 255.0F);
        int alpha = (int) (a * 255.0F);

        for (int d = 0; d < 6; d++) {
            RANDOM.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(state, DIRECTIONS[d], RANDOM);
            int qSize = quads.size();
            for (int i = 0; i < qSize; i++) {
                renderGhostQuad(poseMatrix, normalMatrix, consumer, quads.get(i), red, green, blue, alpha, light);
            }
        }

        RANDOM.setSeed(42L);
        List<BakedQuad> unculledQuads = model.getQuads(state, null, RANDOM);
        int unculledSize = unculledQuads.size();
        for (int i = 0; i < unculledSize; i++) {
            renderGhostQuad(poseMatrix, normalMatrix, consumer, unculledQuads.get(i), red, green, blue, alpha, light);
        }
    }

    private static void renderGhostQuad(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer consumer, BakedQuad quad, int red, int green, int blue, int alpha, int light) {
        int[] vertices = quad.getVertices();
        int vertexCount = vertices.length / 8;

        for (int i = 0; i < vertexCount; i++) {
            int offset = i * 8;
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);

            int packedNormal = vertices[offset + 7];
            float nx, ny, nz;
            if (packedNormal != 0) {
                nx = ((byte) (packedNormal & 0xFF)) / 127.0F;
                ny = ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0F;
                nz = ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0F;
            } else {
                Direction dir = quad.getDirection();
                nx = dir.getStepX();
                ny = dir.getStepY();
                nz = dir.getStepZ();
            }

            consumer.addVertex(poseMatrix, x, y, z)
                    .setColor(red, green, blue, alpha)
                    .setUv(u, v)
                    .setLight(light)
                    .setNormal(nx, ny, nz)
                    ;
        }
    }

    private static void renderGridPreview(PoseStack poseStack, Minecraft mc, Player player, Level level, Camera camera, CompoundTag tag, BlockHitResult blockHit) {
        int gridSize = tag.contains("GridSize") ? tag.getInt("GridSize") : 3;
        if (gridSize < 2) gridSize = 3;
        boolean isBannerMode = tag.contains("IsBannerMode") ? tag.getBoolean("IsBannerMode") : true;
        String gridText = tag.getString("GridText");

        BlockPos hitPos = blockHit.getBlockPos();
        Direction hitFace = blockHit.getDirection();
        BlockState hitState = level.getBlockState(hitPos);

        boolean clickedIsStampable = (hitState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)
                || (hitState.getBlock() instanceof BackplateBlock)
                || (hitState.getBlock() instanceof LetterBlock);

        if (hitFace.getAxis() == Direction.Axis.Y && !clickedIsStampable) {
            return;
        }

        Direction effectiveFace = hitFace;
        if (hitFace.getAxis() == Direction.Axis.Y && clickedIsStampable) {
            if (hitState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                effectiveFace = hitState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        }

        boolean isVertical = tag.getBoolean("IsVertical");
        Vec3 hitLoc = blockHit.getLocation();
        int targetLine;
        if (isVertical) {
            targetLine = SignBlueprintItem.getHitSubCellCol(effectiveFace, hitLoc, gridSize);
        } else {
            targetLine = SignBlueprintItem.getHitSubCellRow(hitLoc, gridSize);
        }

        int numBlocks;
        java.util.List<java.util.Map<Integer, String>> blocksCells = new java.util.ArrayList<>();

        if (isBannerMode && !gridText.isEmpty()) {
            int charsPerBlock = gridSize;
            numBlocks = (gridText.length() + charsPerBlock - 1) / charsPerBlock;
            for (int b = 0; b < numBlocks; b++) {
                blocksCells.add(SignBlueprintItem.getBannerCellsForBlock(gridText, b, gridSize, isVertical, targetLine));
            }
        } else {
            numBlocks = 1;
            java.util.Map<Integer, String> cells = new java.util.HashMap<>();
            if (tag.contains("GridCells", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag list = tag.getList("GridCells", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag ctag = list.getCompound(i);
                    int idx = ctag.getInt("Index");
                    String cPath = ctag.getString("Char");
                    if (!cPath.isEmpty()) {
                        cells.put(idx, cPath);
                    }
                }
            }
            if (cells.isEmpty() && !gridText.isEmpty()) {
                int max = Math.min(gridText.length(), gridSize * gridSize);
                for (int i = 0; i < max; i++) {
                    char ch = gridText.charAt(i);
                    if (ch != ' ') {
                        String path = SignBlueprintItem.getBlockPathForChar(ch);
                        if (path != null) cells.put(i, path);
                    }
                }
            }
            if (cells.isEmpty()) return;
            blocksCells.add(cells);
        }

        boolean withBackplate = tag.getBoolean("WithBackplate");

        boolean wall = effectiveFace.getAxis() != Direction.Axis.Y;
        int stepRotation = wall
                ? SignRotation.fromDirection(effectiveFace)
                : SignRotation.fromDirection(player.getDirection().getCounterClockWise());
        int rightX = SignRotation.horizontalStepX(stepRotation, wall);
        int rightZ = SignRotation.horizontalStepZ(stepRotation, wall);

        BlockPos baseTargetPos = clickedIsStampable ? hitPos : hitPos.relative(hitFace);

        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        for (int b = 0; b < numBlocks; b++) {
            BlockPos targetPos = (isBannerMode && isVertical)
                    ? baseTargetPos.below(b)
                    : offsetRight(baseTargetPos, rightX, rightZ, b);
            BlockState targetState = level.getBlockState(targetPos);

            boolean isBackplate = targetState.getBlock() instanceof BackplateBlock;
            boolean isLetterWithBackplate = false;
            if (targetState.getBlock() instanceof LetterBlock) {
                if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe;
                    if (lbe.isDummy()) {
                        net.minecraft.world.level.block.entity.BlockEntity me = level.getBlockEntity(lbe.getMasterPos());
                        if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) effective = masterBe;
                    }
                    isLetterWithBackplate = effective.hasBackplate();
                }
            }
            boolean isGridSign = targetState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock;
            boolean stampExisting = isGridSign || isBackplate || isLetterWithBackplate;

            boolean canPlace = stampExisting || targetState.canBeReplaced();

            AttachFace attachFace = AttachFace.WALL;
            Direction facing = effectiveFace;
            int facingRotation = SignRotation.fromDirection(facing);

            if (stampExisting) {
                if (targetState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                    attachFace = targetState.getValue(BlockStateProperties.ATTACH_FACE);
                }
                if (targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    facing = targetState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                }
                if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                    facingRotation = gbe.getFacingRotation();
                } else if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                    facingRotation = lbe.getFacingRotation();
                } else {
                    facingRotation = SignRotation.fromDirection(facing);
                }
            } else {
                attachFace = AttachFace.WALL;
                facing = effectiveFace;
                facingRotation = SignRotation.fromDirection(facing);
            }

            boolean isBack = false;
            if (stampExisting && attachFace != AttachFace.WALL) {
                double side = (player.getX() - (targetPos.getX() + 0.5)) * SignRotation.facingX(facingRotation)
                        + (player.getZ() - (targetPos.getZ() + 0.5)) * SignRotation.facingZ(facingRotation);
                if (side < 0.0) isBack = true;
            }
            Direction effectiveFacing = isBack ? facing.getOpposite() : facing;

            poseStack.pushPose();
            poseStack.translate(targetPos.getX() - camPos.x, targetPos.getY() - camPos.y, targetPos.getZ() - camPos.z);

            int rotationDegrees = SignRotation.deltaDegrees(facingRotation, facing);
            if (rotationDegrees != 0) {
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDegrees));
                poseStack.translate(-0.5, 0.0, -0.5);
            }

            if (withBackplate && !stampExisting) {
                BlockState plateState = ModBlocks.BACKPLATE.get().defaultBlockState()
                        .setValue(BackplateBlock.FACING, facing)
                        .setValue(BackplateBlock.FACE, attachFace);
                BakedModel plateModel = mc.getBlockRenderer().getBlockModel(plateState);
                float bpR = canPlace ? 0.85F : 1.0F;
                float bpG = canPlace ? 0.85F : 0.3F;
                float bpB = canPlace ? 0.85F : 0.3F;
                renderGhostModel(poseStack, consumer, plateModel, plateState, bpR, bpG, bpB, 0.4F, 15728880);
            }

            org.joml.Vector3f baseTrans = com.boran.signbuilder.client.grid.GridOffsetHelper.getBaseTranslation(attachFace, effectiveFacing);
            poseStack.translate(baseTrans.x, baseTrans.y, baseTrans.z);

            float scale = 1.0f / gridSize;
            boolean hasPlate = withBackplate || stampExisting;

            float charR = canPlace ? 0.4F : 1.0F;
            float charG = canPlace ? 0.9F : 0.2F;
            float charB = canPlace ? 1.0F : 0.2F;

            int max = gridSize * gridSize;
            java.util.Map<Integer, String> cellMap = blocksCells.get(b);
            for (java.util.Map.Entry<Integer, String> entry : cellMap.entrySet()) {
                int index = entry.getKey();
                if (index < 0 || index >= max) continue;
                String path = entry.getValue();
                Block letterBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("signbuilder", path));
                if (letterBlock == null || letterBlock == net.minecraft.world.level.block.Blocks.AIR) continue;

                BlockState letterState = letterBlock.defaultBlockState();
                if (letterState.hasProperty(LetterBlock.FACING)) letterState = letterState.setValue(LetterBlock.FACING, effectiveFacing);
                if (letterState.hasProperty(LetterBlock.FACE)) letterState = letterState.setValue(LetterBlock.FACE, AttachFace.WALL);

                int col = index % gridSize;
                int row = index / gridSize;
                float colShift = (gridSize - 1) / 2.0f - col;
                float rowShift = (gridSize - 1) / 2.0f - row;
                if (rotationDegrees != 0 && attachFace != AttachFace.WALL) {
                    colShift *= 1.4142135f;
                }

                org.joml.Vector3f offsets = com.boran.signbuilder.client.grid.GridOffsetHelper.getOffset(attachFace, effectiveFacing, colShift, rowShift, scale);
                org.joml.Vector3f cl = hasPlate ? com.boran.signbuilder.client.grid.GridOffsetHelper.getClearance(attachFace, effectiveFacing, 1.05f / 16.0f) : new org.joml.Vector3f(0, 0, 0);
                org.joml.Vector3f c = com.boran.signbuilder.client.grid.GridOffsetHelper.getCenter(attachFace, effectiveFacing);

                poseStack.pushPose();
                poseStack.translate(offsets.x + cl.x, offsets.y + cl.y, offsets.z + cl.z);
                poseStack.translate(c.x, c.y, c.z);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-c.x, -c.y, -c.z);

                BakedModel letterModel = mc.getBlockRenderer().getBlockModel(letterState);
                renderGhostModel(poseStack, consumer, letterModel, letterState, charR, charG, charB, 0.65F, 15728880);

                poseStack.popPose();
            }

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.translucent());
    }
}
