package io.github.aratakileo.emogg.util;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

/**
 * This implementation was borrowed from the fabric API for compatibility with Quilt
 */
public interface HudRenderCallback {
	Event<HudRenderCallback> EVENT = EventFactory.createArrayBacked(HudRenderCallback.class, (listeners) -> (matrixStack, delta) -> {
		for (HudRenderCallback event : listeners) {
			event.onHudRender(matrixStack, delta);
		}
	});

	/**
	 * Called after rendering the whole hud, which is displayed in game, in a world.
	 *
	 * @param drawContext the {@link GuiGraphics} instance
	 * @param tickDelta Progress for linearly interpolating between the previous and current game state
	 */
	void onHudRender(GuiGraphics drawContext, float tickDelta);
}