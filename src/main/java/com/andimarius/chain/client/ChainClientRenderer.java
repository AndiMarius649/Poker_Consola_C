package com.andimarius.chain.client;

import com.andimarius.chain.ChainedPlayersMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

/**
 * Randare simplă de tip "lanț" între doi jucători.
 * Desenăm o linie 3D pe stage-ul de nivel.
 */
@Mod.EventBusSubscriber(modid = ChainedPlayersMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChainClientRenderer {

    private ChainClientRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        for (Map.Entry<UUID, UUID> entry : ClientChainState.snapshot().entrySet()) {
            UUID a = entry.getKey();
            UUID b = entry.getValue();

            // Evităm desenarea dublă pentru aceeași pereche.
            if (a.compareTo(b) >= 0) {
                continue;
            }

            AbstractClientPlayer playerA = findPlayer(level, a);
            AbstractClientPlayer playerB = findPlayer(level, b);
            if (playerA == null || playerB == null) {
                continue;
            }

            Vec3 posA = playerA.getPosition(event.getPartialTick()).add(0.0D, 1.0D, 0.0D);
            Vec3 posB = playerB.getPosition(event.getPartialTick()).add(0.0D, 1.0D, 0.0D);

            drawLine(poseStack, lines,
                    posA.x - cameraPos.x, posA.y - cameraPos.y, posA.z - cameraPos.z,
                    posB.x - cameraPos.x, posB.y - cameraPos.y, posB.z - cameraPos.z);
        }

        bufferSource.endBatch(RenderType.lines());
    }

    private static AbstractClientPlayer findPlayer(ClientLevel level, UUID id) {
        for (AbstractClientPlayer player : level.players()) {
            if (player.getUUID().equals(id)) {
                return player;
            }
        }
        return null;
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer buffer,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2) {
        PoseStack.Pose pose = poseStack.last();

        buffer.vertex(pose.pose(), (float) x1, (float) y1, (float) z1)
                .color(220, 210, 80, 255)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();

        buffer.vertex(pose.pose(), (float) x2, (float) y2, (float) z2)
                .color(220, 210, 80, 255)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
