package pextystudios.emogg;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Emoji {
    private final String name;
    private final Identifier identifier;
    private int width = -1, height = -1;

    public Emoji(String name) {
        this(name, "emoji/" + name + ".png");
    }

    public Emoji(Identifier identifier) {
        this(
                identifier.getPath()
                        .transform(name -> name.substring(name.lastIndexOf('/') + 1))
                        .transform(name -> name.substring(0, name.lastIndexOf('.'))),
                identifier
        );
    }

    public Emoji(String name, String fileName) {
        this(name, new Identifier(Emogg.NAMESPACE + ":" +  fileName.replace('\\', '/')));
    }

    public Emoji(String name, Identifier identifier) {
        this.name = name.replaceAll("-+| +|\\.+", "_");
        this.identifier = identifier;

        try {
            Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(getTextureIdentifier());
            BufferedImage bufferedImage = ImageIO.read(resource.getInputStream());

            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();

            resource.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: \"" + identifier.getPath() + '"', e);
        }
    }

    public boolean match(String string, int charIndex){
        String code = getCode();

        for(int i = 0; i < code.length(); i++){
            int stringIndex = charIndex - i;
            int codeIndex = code.length() - 1 - i;
            if(stringIndex < 0 || codeIndex < 0) return false;
            if(string.charAt(stringIndex) != code.charAt(codeIndex)) return false;
        }
        return true;
    }

    public void draw(
            MatrixStack matrixStack,
            float x,
            float y,
            float size,
            float alpha
    ) {
        float scaleX = (float) this.width / this.height * 1.5f, scaleY = 1.5f;

        scaleX = Math.round(size * scaleX) / size;
        scaleY = Math.round(size * scaleY) / size;
        int corrected_x = (int)(x + size * (1.0F - scaleX) / 2.0F);
        int corrected_y = (int)(y + size * (1.0F - scaleY) / 2.0F);
        int shaderTexture = RenderSystem.getShaderTexture(0);

        RenderSystem.setShaderTexture(0, this.getTextureIdentifier());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        DrawableHelper.drawTexture(
                matrixStack,
                corrected_x,
                corrected_y,
                Math.round(size * scaleX),
                Math.round(size * scaleY),
                0.0F,
                0.0F,
                width,
                height,
                width,
                height
        );

        RenderSystem.setShaderTexture(0, shaderTexture);
    }

    public String getName() {return name;}

    public String getCode() {return ':' + this.name + ':';}

    public Identifier getTextureIdentifier() {return this.identifier;}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return '{' + name + ": " + identifier.getPath() + '}';
    }
}
