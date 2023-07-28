package pextystudios.emogg.mixin.accessor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(Font.class)
public interface FontAccessor {
    @Accessor
    Function<ResourceLocation, FontSet> getFonts();

    @Accessor
    boolean isFilterFishyGlyphs();

    @Invoker
    FontSet callGetFontSet(ResourceLocation $$0);

    @Invoker
    void callRenderChar(BakedGlyph $$0, boolean $$1, boolean $$2, float $$3, float $$4, float $$5, Matrix4f $$6, VertexConsumer $$7, float $$8, float $$9, float $$10, float $$11, int $$12);

    @Accessor
    static Vector3f getSHADOW_OFFSET() {
        throw new AssertionError();
    }
}
