package com.sammy.lodestone.systems.rendering.renderlayer;

import net.minecraft.client.render.ShaderProgram;

public interface ShaderUniformHandler {

	ShaderUniformHandler LUMITRANSPARENT = instance -> {
		instance.getUniformOrDefault("LumiTransparency").setInt(1);
	};

	void updateShaderData(ShaderProgram instance);
}
