package com.sammy.lodestone.systems.rendering.shader;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sammy.lodestone.systems.rendering.UniformData;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShaderHolder {

	public ExtendedShader instance;
	public ArrayList<String> uniforms;
	public ArrayList<UniformData> defaultUniformData = new ArrayList<>();
	public final RenderPhase.Shader phase = new RenderPhase.Shader(getInstance());

	public ShaderHolder(String... uniforms) {
		this.uniforms = new ArrayList<>(List.of(uniforms));
	}

	public void setUniformDefaults() {
		RenderSystem.setShaderTexture(1, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		defaultUniformData.forEach(u -> u.setUniformValue(instance.getUniformOrDefault(u.uniformName)));
	}

	public void setInstance(ExtendedShader instance) {
		this.instance = instance;
	}

	public Supplier<ShaderProgram> getInstance() {
		return () -> instance;
	}
}
