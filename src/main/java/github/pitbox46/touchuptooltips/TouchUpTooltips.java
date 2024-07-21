package github.pitbox46.touchuptooltips;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
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
        private static ItemStack hoveredStack = ItemStack.EMPTY;
        private static long scroll = 0;
        private static float partialTicks = 0;
        //Used as a flag to determine if the tooltip autoscroll should be reset
        private static int shouldResetScroll = 0;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            //Tick the autoscroller
            scroll += 1;
        }

        @SubscribeEvent
        public static void onRenderGUI(RenderGuiEvent.Pre event) {
            //Get partial ticks for tooltip render later
            partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            shouldResetScroll++;
        }

        /**
         * Modified from {@link GuiGraphics#renderTooltipInternal(Font, List, int, int, ClientTooltipPositioner)}
         * @param event event
         */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRenderTooltip(RenderTooltipEvent.Pre event) {
            event.setCanceled(true);

            GuiGraphics gui = event.getGraphics();
            ItemStack tooltipStack = event.getItemStack();
            List<ClientTooltipComponent> components = event.getComponents();

            if (shouldResetScroll > 1) {
                scroll = 0;
            }
            shouldResetScroll = 0;
            if (hoveredStack != tooltipStack) {
                hoveredStack = tooltipStack;
                scroll = 0;
            }

            ClientTooltipPositioner tooltipPositioner = (screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
                Vector2ic pos = event.getTooltipPositioner().positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
                return new Vector2i(pos.x(), Math.max(pos.y(), 4));
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
            final int tipWidthEffective = tipWidth;
            final int tipHeightEffective = Math.min(tipHeight, gui.guiHeight() - 8);
            int heightDiff = tipHeight - tipHeightEffective;
            Vector2ic vector2ic = tooltipPositioner.positionTooltip(gui.guiWidth(), gui.guiHeight(), event.getX(), event.getY(), tipWidth, tipHeight);
            final int startX = vector2ic.x();
            final int startY = vector2ic.y();

            gui.pose().pushPose();

            //Scaler
            float scale = 1;
            if (Config.SCALE.get()) {
                scale = (float) tipHeightEffective / tipHeight;
                if (scale < Config.SCALE_MAX.get()) {
                    scale = Config.SCALE_MAX.get().floatValue();
                }
                gui.pose().translate(startX * (1 - scale), 0, 0);
                gui.pose().scale(scale, scale, 1);

                heightDiff = (int) (tipHeight * scale - tipHeightEffective);
            }

            //Autoscroller
            float scrollAmount = 0;
            if (Config.SCROLL.get()) {
                double speed = Config.SCROLL_SPEED.get() * scale;
                scrollAmount = heightDiff > 0 ? (float) ((scroll + partialTicks) * speed - Config.SCROLL_WAIT_TOP.get()):0;
                if (!Config.SCROLL.get() || scrollAmount < 0) {
                    scrollAmount = 0;
                } else if (scrollAmount > heightDiff) {
                    if (scrollAmount > heightDiff + Config.SCROLL_WAIT_BOTTOM.get()) {
                        scroll = 0;
                    }
                    scrollAmount = heightDiff;
                }
            }

            net.neoforged.neoforge.client.event.RenderTooltipEvent.Color colorEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipColor(tooltipStack, gui, startX, startY, event.getFont(), components);
            int finalTipHeight = tipHeight;
            int z = 400;
            gui.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(gui, startX, startY, tipWidthEffective, Config.SCALE.get() ? finalTipHeight: tipHeightEffective, z, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd()));
            gui.pose().translate(0.0F, -scrollAmount / scale, z);
            //Scissor the text to fit inside the tooltip box
            gui.enableScissor(0, 2, gui.guiWidth(), gui.guiHeight() - 2);
            int currentY = startY;

            for (int i = 0; i < components.size(); i++) {
                ClientTooltipComponent clienttooltipcomponent1 = components.get(i);
                if (clienttooltipcomponent1 instanceof ClientTextTooltip tooltip) {
                    gui.drawString(event.getFont(), tooltip.text, startX, currentY, -1);
                } else {
                    //This doesn't get scissored for some reason
                    clienttooltipcomponent1.renderText(event.getFont(), startX, currentY, gui.pose().last().pose(), gui.bufferSource());
                }
                currentY += clienttooltipcomponent1.getHeight() + (i == 0 ? 2 : 0);
            }

            currentY = startY;

            for (int i = 0; i < components.size(); i++) {
                ClientTooltipComponent clienttooltipcomponent2 = components.get(i);
                clienttooltipcomponent2.renderImage(event.getFont(), startX, currentY, gui);
                currentY += clienttooltipcomponent2.getHeight() + (i == 0 ? 2 : 0);
            }
            gui.disableScissor();
            gui.pose().popPose();
        }

        @SubscribeEvent
        public static void onTooltipColor(RenderTooltipEvent.Color event) {
            event.setBackgroundStart(Config.BACKGROUND_COLOR1_INT.get());
            event.setBackgroundEnd(Config.BACKGROUND_COLOR2_INT.get());
            event.setBorderStart(Config.BORDER_COLOR1_INT.get());
            event.setBorderEnd(Config.BORDER_COLOR2_INT.get());
        }
    }
}
