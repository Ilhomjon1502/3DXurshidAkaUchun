package uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.objects;

import uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.model.Object3DData;
import uz.ilhomjon.a3dxurshidakauchun.util.io.IOUtils;

import android.opengl.GLES20;

public final class Line {

    public static Object3DData build(float[] line) {
        return new Object3DData(IOUtils.createFloatBuffer(line.length).put(line))
                .setDrawMode(GLES20.GL_LINES).setId("Line");
    }
}
