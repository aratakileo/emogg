package pextystudios.emogg.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import pextystudios.emogg.Emogg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class ResourceUtil {
    public static void processModResources(
            String pathPrefix,
            Predicate<String> condition,
            Consumer<ResourceLocation> processor
    ) {
        for (var pack: Minecraft.getInstance().getResourcePackRepository().getSelectedPacks())
            for (var resourceLocation: pack.open().getResources(
                    PackType.CLIENT_RESOURCES,
                    Emogg.NAMESPACE,
                    pathPrefix,
                    Integer.MAX_VALUE,
                    condition
            ))
                processor.accept(resourceLocation);
    }

    public interface ZipEntryProcessor {
        void process(ZipEntry zipEntry, InputStream inputStream, String fileName, boolean isDirectory);
    }

    public static void processZipFile(String path, ZipEntryProcessor processor) throws IOException {
        var zipFile = new ZipFile(path);
        var fileInputStream = new FileInputStream(path);
        var zipInputStream = new ZipInputStream(fileInputStream);

        ZipEntry entry;

        while ((entry = zipInputStream.getNextEntry()) != null)
            processor.process(entry, zipFile.getInputStream(entry), entry.getName(), entry.isDirectory());

        zipInputStream.close();
        fileInputStream.close();
        zipFile.close();
    }
}
