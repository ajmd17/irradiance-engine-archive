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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Setter;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Uniform;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import javax.swing.JOptionPane;

public class DefaultShader extends BaseShader {
	public static class Config {
		/** The uber vertex shader to use, null to use the default vertex shader. */
		public String vertexShader = null;
		/** The uber fragment shader to use, null to use the default fragment shader. */
		public String fragmentShader = null;
		/** The number of directional lights to use */
		public int numDirectionalLights = 2;
		/** The number of point lights to use */
		public int numPointLights = 5;
		/** The number of spot lights to use */
		public int numSpotLights = 0;
		/** The number of bones to use */
		public int numBones = 12;
		/** */
		public boolean ignoreUnimplemented = true;
		/** Set to 0 to disable culling, -1 to inherit from {@link DefaultShader#defaultCullFace} */
		public int defaultCullFace = -1;
		/** Set to 0 to disable depth test, -1 to inherit from {@link DefaultShader#defaultDepthFunc} */
		public int defaultDepthFunc = -1;

		public Config () {
		}

		public Config (final String vertexShader, final String fragmentShader) {
			this.vertexShader = vertexShader;
			this.fragmentShader = fragmentShader;
		}
	}

	public static class Inputs {
		public final static Uniform projTrans = new Uniform("u_projTrans");
		public final static Uniform viewTrans = new Uniform("u_viewTrans");
		public final static Uniform projViewTrans = new Uniform("u_projViewTrans");
		public final static Uniform cameraPosition = new Uniform("u_cameraPosition");
		public final static Uniform cameraDirection = new Uniform("u_cameraDirection");
		public final static Uniform cameraUp = new Uniform("u_cameraUp");

		public final static Uniform worldTrans = new Uniform("u_worldTrans");
		public final static Uniform viewWorldTrans = new Uniform("u_viewWorldTrans");
		public final static Uniform projViewWorldTrans = new Uniform("u_projViewWorldTrans");
		public final static Uniform normalMatrix = new Uniform("u_normalMatrix");
		public final static Uniform bones = new Uniform("u_bones");

		public final static Uniform shininess = new Uniform("u_shininess", FloatAttribute.Shininess);
		public final static Uniform opacity = new Uniform("u_opacity", BlendingAttribute.Type);
		public final static Uniform diffuseColor = new Uniform("u_diffuseColor", ColorAttribute.Diffuse);
		public final static Uniform diffuseTexture = new Uniform("u_diffuseTexture", TextureAttribute.Diffuse);
		public final static Uniform specularColor = new Uniform("u_specularColor", ColorAttribute.Specular);
		public final static Uniform specularTexture = new Uniform("u_specularTexture", TextureAttribute.Specular);
		public final static Uniform emissiveColor = new Uniform("u_emissiveColor", ColorAttribute.Emissive);
		public final static Uniform reflectionColor = new Uniform("u_reflectionColor", ColorAttribute.Reflection);
		public final static Uniform normalTexture = new Uniform("u_normalTexture", TextureAttribute.Normal);
		public final static Uniform alphaTest = new Uniform("u_alphaTest", FloatAttribute.AlphaTest);

		public final static Uniform environmentCubemap = new Uniform("u_environmentCubemap");
	}

	public static class Setters {
		public final static Setter projTrans = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.projection);
			}
		};
		public final static Setter viewTrans = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.view);
			}
		};
		public final static Setter projViewTrans = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.combined);
			}
		};
		public final static Setter cameraPosition = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.position.x, shader.camera.position.y, shader.camera.position.z,
					1.1881f / (shader.camera.far * shader.camera.far));
			}
		};
		public final static Setter cameraDirection = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.direction);
			}
		};
		public final static Setter cameraUp = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return true;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.up);
			}
		};
		public final static Setter worldTrans = new Setter() {
			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return false;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, renderable.worldTransform);
			}
		};
		public final static Setter viewWorldTrans = new Setter() {
			final Matrix4 temp = new Matrix4();

			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return false;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, temp.set(shader.camera.view).mul(renderable.worldTransform));
			}
		};
		public final static Setter projViewWorldTrans = new Setter() {
			final Matrix4 temp = new Matrix4();

			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return false;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, temp.set(shader.camera.combined).mul(renderable.worldTransform));
			}
		};
		public final static Setter normalMatrix = new Setter() {
			private final Matrix3 tmpM = new Matrix3();

			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return false;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, tmpM.set(renderable.worldTransform).inv().transpose());
			}
		};

		public static class Bones implements Setter {
			private final static Matrix4 idtMatrix = new Matrix4();
			public final float bones[];

			public Bones (final int numBones) {
				this.bones = new float[numBones * 16];
			}

			@Override
			public boolean isGlobal (BaseShader shader, int inputID) {
				return false;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				for (int i = 0; i < bones.length; i++) {
					final int idx = i / 16;
					bones[i] = (renderable.bones == null || idx >= renderable.bones.length || renderable.bones[idx] == null) ? idtMatrix.val[i % 16]
						: renderable.bones[idx].val[i % 16];
				}
				shader.program.setUniformMatrix4fv(shader.loc(inputID), bones, 0, bones.length);
			}
		}

		

		

		
	}

	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.internal("data/shaders/default/default.vertex.glsl").readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.internal("data/shaders/default/default.fragment.glsl").readString();
		return defaultFragmentShader;
	}

	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse | ColorAttribute.Diffuse
		| ColorAttribute.Specular | FloatAttribute.Shininess;

	/** @deprecated Replaced by {@link Config#defaultCullFace} Set to 0 to disable culling */
	@Deprecated public static int defaultCullFace = GL20.GL_BACK;
	/** @deprecated Replaced by {@link Config#defaultDepthFunc} Set to 0 to disable depth test */
	@Deprecated public static int defaultDepthFunc = GL20.GL_LEQUAL;

	// Global uniforms
	public final int u_projTrans;
	public final int u_viewTrans;
	public final int u_projViewTrans;
	public final int u_cameraPosition;
	public final int u_cameraDirection;
	public final int u_cameraUp;
	public final int u_time;
	// Object uniforms
	public final int u_worldTrans;
	public final int u_viewWorldTrans;
	public final int u_projViewWorldTrans;
	public final int u_normalMatrix;
	public final int u_bones;
	// Material uniforms
	
	// Lighting uniforms
	
	
	protected final int u_shadowMapProjViewTrans = register(new Uniform("u_shadowMapProjViewTrans"));
	protected final int u_shadowTexture = register(new Uniform("u_shadowTexture"));
	protected final int u_shadowPCFOffset = register(new Uniform("u_shadowPCFOffset"));
	// FIXME Cache vertex attribute locations...


	protected final boolean lighting;
	protected final boolean environmentCubemap;
	protected final boolean shadowMap;
	protected final boolean fog;
	protected final AmbientCubemap ambientCubemap = new AmbientCubemap();
	

	/** The renderable used to create this shader, invalid after the call to init */
	private Renderable renderable;
	private long materialMask;
	private long vertexMask;
	protected final Config config;
	/** Material attributes which are not required but always supported. */
	private final static long optionalAttributes = IntAttribute.CullFace | DepthTestAttribute.Type;
        
	public DefaultShader (final Renderable renderable) {
		this(renderable, new Config());
	}

	public DefaultShader (final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public DefaultShader (final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
			config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
	}

	public DefaultShader (final Renderable renderable, final Config config, final String prefix, final String vertexShader,
		final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}

	public DefaultShader (final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		this.config = config;
		this.program = shaderProgram;
		this.lighting = renderable.environment != null;
		this.environmentCubemap = renderable.material.has(CubemapAttribute.EnvironmentMap)
			|| (lighting && renderable.environment.has(CubemapAttribute.EnvironmentMap));
		this.shadowMap = lighting && renderable.environment.shadowMap != null;
		this.fog = lighting && renderable.environment.has(ColorAttribute.Fog);
		this.renderable = renderable;
		materialMask = renderable.material.getMask() | optionalAttributes;
		vertexMask = renderable.mesh.getVertexAttributes().getMask();

		

		// Global uniforms
		u_projTrans = register(Inputs.projTrans, Setters.projTrans);
		u_viewTrans = register(Inputs.viewTrans, Setters.viewTrans);
		u_projViewTrans = register(Inputs.projViewTrans, Setters.projViewTrans);
		u_cameraPosition = register(Inputs.cameraPosition, Setters.cameraPosition);
		u_cameraDirection = register(Inputs.cameraDirection, Setters.cameraDirection);
		u_cameraUp = register(Inputs.cameraUp, Setters.cameraUp);
		u_time = register(new Uniform("u_time"));
		// Object uniforms
		u_worldTrans = register(Inputs.worldTrans, Setters.worldTrans);
		u_viewWorldTrans = register(Inputs.viewWorldTrans, Setters.viewWorldTrans);
		u_projViewWorldTrans = register(Inputs.projViewWorldTrans, Setters.projViewWorldTrans);
		u_normalMatrix = register(Inputs.normalMatrix, Setters.normalMatrix);
		u_bones = (renderable.bones != null && config.numBones > 0) ? register(Inputs.bones, new Setters.Bones(config.numBones))
			: -1;

		
	}

	@Override
	public void init () {
		final ShaderProgram prog = this.program;
		this.program = null;
		init(prog, renderable);
		renderable = null;

	}

	private static final boolean and (final long mask, final long flag) {
		return (mask & flag) == flag;
	}

	private static final boolean or (final long mask, final long flag) {
		return (mask & flag) != 0;
	}

	public static String createPrefix (final Renderable renderable, final Config config) {
		String prefix = "";
		final long mask = renderable.material.getMask();
		final long attributes = renderable.mesh.getVertexAttributes().getMask();
		if (and(attributes, Usage.Position)) prefix += "#define positionFlag\n";
		
                if (renderable.environment.shadowMap != null) prefix += "#define shadowMapFlag\n";
		final int n = renderable.mesh.getVertexAttributes().size();
		
		return prefix;
	}

	@Override
	public boolean canRender (final Renderable renderable) {
		return (materialMask == (renderable.material.getMask() | optionalAttributes))
			&& (vertexMask == renderable.mesh.getVertexAttributes().getMask()) && (renderable.environment != null) == lighting
			&& ((renderable.environment != null && renderable.environment.has(ColorAttribute.Fog)) == fog);
	}

	@Override
	public int compareTo (Shader other) {
		if (other == null) return -1;
		if (other == this) return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}

	@Override
	public boolean equals (Object obj) {
		return (obj instanceof DefaultShader) ? equals((DefaultShader)obj) : false;
	}

	public boolean equals (DefaultShader obj) {
		return (obj == this);
	}

	private Matrix3 normalMatrix = new Matrix3();
	private Camera camera;
	private float time;
	private boolean lightsSet;

	@Override
	public void begin (final Camera camera, final RenderContext context) {
		super.begin(camera, context);

		

//		if (has(u_time)) set(u_time, time += Gdx.graphics.getDeltaTime());
	}

	@Override
	public void render (final Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
               // Gdx.gl.glEnable(GL20.GL_CULL_FACE);
               // Gdx.gl.glCullFace(GL20.GL_FRONT);
		super.render(renderable);
	}

	@Override
	public void end () {
		currentMaterial = null;
		super.end();
	}

	Material currentMaterial;

	

	private final Vector3 tmpV1 = new Vector3();

	

	@Override
	public void dispose () {
		program.dispose();
		super.dispose();
	}

}