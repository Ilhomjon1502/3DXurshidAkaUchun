package uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.drawer;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import uz.ilhomjon.a3dxurshidakauchun.R;
import uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.model.AnimatedModel;
import uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.model.Object3DData;
import uz.ilhomjon.a3dxurshidakauchun.util.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2013-2020 andresoviedo.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
public class RendererFactory {

    /**
     * shader code loaded from raw resources
     * resources are cached on activity thread
     */
    private Map<String, String> shadersCode = new HashMap<>();
    /**
     * list of opengl drawers
     */
    private Map<Shader, GLES20Renderer> drawers = new HashMap<>();

    public RendererFactory(Context context) throws IllegalAccessException, IOException {

        Log.i("RendererFactory", "Discovering shaders...");
        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            String shaderId = fields[count].getName();
            Log.v("RendererFactory", "Loading shader... " + shaderId);
            int shaderResId = fields[count].getInt(fields[count]);
            byte[] shaderBytes = IOUtils.read(context.getResources().openRawResource(shaderResId));
            String shaderCode = new String(shaderBytes);
            shadersCode.put(shaderId, shaderCode);
        }
        Log.i("RendererFactory", "Shaders loaded: " + shadersCode.size());
    }

    public Renderer getDrawer(Object3DData obj, boolean usingSkyBox, boolean usingTextures, boolean usingLights, boolean usingAnimation, boolean drawColors) {

        // double check features
        boolean isAnimated = usingAnimation && obj instanceof AnimatedModel
                && ((AnimatedModel) obj).getAnimation() != null && (((AnimatedModel) obj).getAnimation()).isInitialized();
        boolean isUsingLights = usingLights && (obj.getNormalsBuffer() != null || obj.getNormalsBuffer() != null);
        boolean isTextured = usingTextures && obj.getTextureData() != null && obj.getTextureBuffer() != null;
        boolean isColoured = drawColors && obj != null && (obj.getColorsBuffer() != null || obj
                .getColorsBuffer() != null);

        final Shader shader = getShader(usingSkyBox, isAnimated, isUsingLights, isTextured, isColoured);

        // get cached drawer
        GLES20Renderer drawer = drawers.get(shader);
        if (drawer != null) return drawer;

        // build drawer
        String vertexShaderCode = shadersCode.get(shader.vertexShaderCode);
        String fragmentShaderCode = shadersCode.get(shader.fragmentShaderCode);
        if (vertexShaderCode == null || fragmentShaderCode == null) {
            Log.e("RendererFactory", "Shaders not found for " + shader.id);
            return null;
        }

        // experimental: inject glPointSize
        vertexShaderCode = vertexShaderCode.replace("void main(){", "void main(){\n\tgl_PointSize = 5.0;");

        // use opengl constant to dynamically set up array size in shaders. That should be >=120
        vertexShaderCode = vertexShaderCode.replace("const int MAX_JOINTS = 60;","const int MAX_JOINTS = gl_MaxVertexUniformVectors > 60 ? 60 : gl_MaxVertexUniformVectors;");

        // create drawer
        Log.v("RendererFactory", "\n---------- Vertex shader ----------\n");
        Log.v("RendererFactory", vertexShaderCode);
        Log.v("RendererFactory", "---------- Fragment shader ----------\n");
        Log.v("RendererFactory", fragmentShaderCode);
        Log.v("RendererFactory", "-------------------------------------\n");
        drawer = GLES20Renderer.getInstance(shader.id, vertexShaderCode, fragmentShaderCode);

        // cache drawer
        drawers.put(shader, drawer);

        // return drawer
        return drawer;
    }

    @NonNull
    private Shader getShader(boolean isUsingSkyBox, boolean isAnimated, boolean isUsingLights, boolean isTextured, boolean
            isColoured) {

        if (isUsingSkyBox){
            return Shader.SKYBOX;
        }

        Shader ret = null;
        if (isAnimated){
            if (isUsingLights){
                if (isTextured){
                    if (isColoured){
                        ret = Shader.ANIM_LIGHT_TEXTURE_COLORS;
                    } else {
                        ret = Shader.ANIM_LIGHT_TEXTURE;
                    }
                } else{
                    if (isColoured){
                        ret = Shader.ANIM_LIGHT_COLORS;
                    } else {
                        ret = Shader.ANIM_LIGHT;
                    }
                }
            } else{
                if (isTextured){
                    if (isColoured){
                        ret = Shader.ANIM_TEXTURE_COLORS;
                    } else {
                        ret = Shader.ANIM_TEXTURE;
                    }
                } else{
                    if (isColoured){
                        ret = Shader.ANIM_COLORS;
                    } else {
                        ret = Shader.ANIM;
                    }
                }
            }
        } else {
            if (isUsingLights){
                if (isTextured){
                    if (isColoured){
                        ret = Shader.LIGHT_TEXTURE_COLORS;
                    } else {
                        ret = Shader.LIGHT_TEXTURE;
                    }
                } else{
                    if (isColoured){
                        ret = Shader.LIGHT_COLORS;
                    } else {
                        ret = Shader.LIGHT;
                    }
                }
            } else{
                if (isTextured){
                    if (isColoured){
                        ret = Shader.TEXTURE_COLORS;
                    } else{
                        ret = Shader.TEXTURE;
                    }
                } else{
                    if (isColoured){
                        ret = Shader.COLORS;
                    } else{
                        ret = Shader.SHADER;
                    }
                }
            }
        }
        return ret;
    }

    public Renderer getBoundingBoxDrawer() {
        return getDrawer(null, false, false, false, false, false);
    }

    public Renderer getFaceNormalsDrawer() {
        return getDrawer(null, false, false, false, false, false);
    }

    public Renderer getBasicShader() {
        return getDrawer(null, false, false, false, false, false);
    }

    public Renderer getSkyBoxDrawer() {
        return getDrawer(null, true, false, false, false, false);
    }
}
