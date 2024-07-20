package github.pitbox46.touchuptooltips;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.slf4j.Logger;

import java.util.List;

@Mod(TouchUpTooltips.MODID)
public class TouchUpTooltips
{
    public static final String MODID = "touchuptooltips";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TouchUpTooltips(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT);
//        NeoForge.EVENT_BUS.register(this);
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
        /**
         * Modified from {@link GuiGraphics#renderTooltipInternal(Font, List, int, int, ClientTooltipPositioner)}
         * @param event
         */
        @SubscribeEvent
        public static void onRenderTooltip(RenderTooltipEvent.Pre event) {
            event.setCanceled(true);

            GuiGraphics gui = event.getGraphics();
            ItemStack tooltipStack = event.getItemStack();
            List<ClientTooltipComponent> components = event.getComponents();
            ClientTooltipPositioner tooltipPositioner = (screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
                Vector2ic pos = event.getTooltipPositioner().positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
                Vector2ic returnPos = new Vector2i(pos.x(), Math.max(pos.y(), 4));
                return returnPos;
            };


            int tipWidth = 0;
            int tipHeight = components.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : components) {
                int componentWidth = clienttooltipcomponent.getWidth(event.getFont());
                if (componentWidth > tipWidth) {
                    tipWidth = componentWidth;
                }

                tipHeight += clienttooltipcomponent.getHeight();
            }

            final int tipWidthFinal = tipWidth;
            final int tipHeightFinal = Math.min(tipHeight, event.getScreenHeight() - 8);
            Vector2ic vector2ic = tooltipPositioner.positionTooltip(gui.guiWidth(), gui.guiHeight(), event.getX(), event.getY(), tipWidth, tipHeight);
            final int startX = vector2ic.x();
            final int startY = vector2ic.y();

            gui.pose().pushPose();
            int z = 400;
            net.neoforged.neoforge.client.event.RenderTooltipEvent.Color colorEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipColor(tooltipStack, gui, startX, startY, event.getFont(), components);
            gui.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(gui, startX, startY, tipWidthFinal, tipHeightFinal, z, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd()));
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
