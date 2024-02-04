package io.github.aratakileo.emogg.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ModMenuConfigScreenGetter implements ModMenuApi {
    public static Function<@Nullable Screen, @NotNull Screen> configScreenGetter;

    @Override
    public @NotNull ConfigScreenFactory<?> getModConfigScreenFactory() {
        return configScreenGetter::apply;
    }
}
