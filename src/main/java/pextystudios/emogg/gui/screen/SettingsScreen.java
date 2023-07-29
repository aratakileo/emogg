package pextystudios.emogg.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import pextystudios.emogg.gui.component.Button;
import pextystudios.emogg.EmoggConfig;

public class SettingsScreen extends AbstractScreen {
    public SettingsScreen() {
        super(Component.translatable("emogg.settings.title"));
    }

    public SettingsScreen(Screen parent) {
        super(Component.translatable("emogg.settings.title"), parent);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(0, 0, Component.translatable("emogg.settings.save_and_quit")) {{
            setOnClicked(button -> onClose());
            x = horizontalCenter() - width / 2;
            y = SettingsScreen.this.height - height - 20;
        }});
    }

    private String getState(boolean state) {
        return "§l" + (state ? "§2" : "§c") + Language.getInstance().getOrDefault("emogg.gui.state." + (state ? "enabled" : "disabled"));
    }

    @Override
    public void onClose() {
        EmoggConfig.save();
        super.onClose();
    }
}
