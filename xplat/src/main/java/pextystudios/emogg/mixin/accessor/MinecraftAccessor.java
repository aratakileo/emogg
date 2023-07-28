package pextystudios.emogg.mixin.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor
    @Mutable
    void setFont(Font font);

    @Accessor
    @Mutable
    void setFontFilterFishy(Font font);
}
