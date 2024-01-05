package io.github.aratakileo.emogg.api;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.aratakileo.emogg.gui.screen.SettingsScreen;
import org.jetbrains.annotations.NotNull;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public @NotNull ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SettingsScreen::new;
    }
}
