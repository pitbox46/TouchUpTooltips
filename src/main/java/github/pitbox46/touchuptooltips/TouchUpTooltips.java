package github.pitbox46.touchuptooltips;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector2ic;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(TouchUpTooltips.MODID)
public class TouchUpTooltips
{
    public static final String MODID = "touchuptooltips";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TouchUpTooltips(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT);
        NeoForge.EVENT_BUS.register(this);
    }

    // Spawns in a sword for testing
//    @SubscribeEvent
//    public void onPlayerJoin(PlayerEvent.PlayerRespawnEvent event) {
//        if (event.getEntity().level().isClientSide) {
//            return;
//        }
//        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
//        sword.update(DataComponents.LORE, ItemLore.EMPTY, lore -> new ItemLore(Util.make(new ArrayList<>(), list -> {
//            for (int i = 0; i < 100; i++) {
//                list.add(Component.literal(String.valueOf(i)));
//            }
//        })));
//        event.getEntity().spawnAtLocation(sword);
//    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRenderTooltip(RenderTooltipEvent.Pre event) {
            event.setCanceled(true);

            GuiGraphics gui = event.getGraphics();
            ItemStack tooltipStack = event.getItemStack();
            List<ClientTooltipComponent> components = event.getComponents();
            ClientTooltipPositioner tooltipPositioner = event.getTooltipPositioner();


            int tpWidth = 0;
            int tpHeight = components.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : components) {
                int componentWidth = clienttooltipcomponent.getWidth(event.getFont());
                if (componentWidth > tpWidth) {
                    tpWidth = componentWidth;
                }

                tpHeight += clienttooltipcomponent.getHeight();
            }

            int i2 = tpWidth;
            int j2 = tpHeight;
            Vector2ic vector2ic = tooltipPositioner.positionTooltip(gui.guiWidth(), gui.guiHeight(), event.getX(), event.getY(), i2, j2);
            int startX = vector2ic.x();
            int startY = vector2ic.y();
            gui.pose().pushPose();
            int z = 400;
            net.neoforged.neoforge.client.event.RenderTooltipEvent.Color colorEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipColor(tooltipStack, gui, startX, startY, event.getFont(), components);
            gui.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(gui, startX, startY, i2, j2, z, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd()));
            gui.pose().translate(0.0F, 0.0F, z);
            int currentY = startY;

            for (int i = 0; i < components.size(); i++) {
                ClientTooltipComponent clienttooltipcomponent1 = components.get(i);
                clienttooltipcomponent1.renderText(event.getFont(), startX, currentY, gui.pose().last().pose(), gui.bufferSource());
                currentY += clienttooltipcomponent1.getHeight() + (i == 0 ? 2 : 0);
            }

            currentY = startY;

            for (int i = 0; i < components.size(); i++) {
                ClientTooltipComponent clienttooltipcomponent2 = components.get(i);
                clienttooltipcomponent2.renderImage(event.getFont(), startX, currentY, gui);
                currentY += clienttooltipcomponent2.getHeight() + (i == 0 ? 2 : 0);
            }

            gui.pose().popPose();
        }
    }
}
