/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.irgames.engine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;

import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class DefaultShaderProvider extends BaseShaderProvider implements ShaderProvider {
	public final NewShadowShader.Config config;

	public DefaultShaderProvider (final NewShadowShader.Config config) {
		this.config = (config == null) ? new NewShadowShader.Config() : config;
	}

	public DefaultShaderProvider (final String vertexShader, final String fragmentShader) {
		this(new NewShadowShader.Config(vertexShader, fragmentShader));
	}

	public DefaultShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	public DefaultShaderProvider () {
		this(null);
	}

	@Override
	protected Shader createShader (final Renderable renderable) {
		return new NewShadowShader(renderable, config);
	}
}