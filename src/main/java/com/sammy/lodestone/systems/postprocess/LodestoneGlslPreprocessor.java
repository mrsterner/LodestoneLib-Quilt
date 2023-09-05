package com.sammy.lodestone.systems.postprocess;

import com.sammy.lodestone.LodestoneLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LodestoneGlslPreprocessor extends GlImportProcessor {

	public static final LodestoneGlslPreprocessor PREPROCESSOR = new LodestoneGlslPreprocessor();

	@Nullable
	@Override
	public String loadImport(boolean inline, String name) {
		LodestoneLib.LOGGER.debug("Loading moj_import in EffectProgram: " + name);

		Identifier id= new Identifier(name);
		Identifier id1 = new Identifier(id.getNamespace(), "shaders/include/" + id.getPath() + ".glsl");

        Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(id1).orElseThrow();

		try {
			return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException ioexception) {
			LodestoneLib.LOGGER.error("Could not open GLSL import {}: {}", name, ioexception.getMessage());
			return "#error " + ioexception.getMessage();
		}
	}
}
