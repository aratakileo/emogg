package pextystudios.emogg.mixin;

import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestor.class)
public interface CommandSuggestorAccessor {
    @Accessor
    TextFieldWidget getTextField();
}

