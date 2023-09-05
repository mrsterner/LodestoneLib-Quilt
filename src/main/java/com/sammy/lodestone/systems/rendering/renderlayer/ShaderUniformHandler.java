package com.sammy.lodestone.systems.rendering.renderlayer;


import net.minecraft.client.gl.ShaderProgram;

public interface ShaderUniformHandler {

	ShaderUniformHandler LUMITRANSPARENT = instance -> {
		instance.getUniformOrDefault("LumiTransparency").set(1);
	};

	void updateShaderData(ShaderProgram instance);
}
