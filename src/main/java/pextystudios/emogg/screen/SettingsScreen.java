package pextystudios.emogg.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import pextystudios.emogg.ConfigContainer;

public class SettingsScreen extends AbstractScreen {
    public SettingsScreen(Screen parent) {
        super(new TextComponent("Emogg settings"), parent);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button(
                (width - 200) / 2,
                (height - 20) / 2,
                200,
                20,
                getBuiltinEmojiSwitcherText(),
                button -> {
                    ConfigContainer.data.isBuiltinEmojiEnabled = !ConfigContainer.data.isBuiltinEmojiEnabled;
                    button.setMessage(getBuiltinEmojiSwitcherText());
                }
        ));
    }

    private Component getBuiltinEmojiSwitcherText() {
        return new TextComponent(
                "Built-in emojis in prompts: " + (ConfigContainer.data.isBuiltinEmojiEnabled ? "Enabled" : "Disabled")
        );
    }

    @Override
    public void onClose() {
        ConfigContainer.save();
        super.onClose();
    }
}
