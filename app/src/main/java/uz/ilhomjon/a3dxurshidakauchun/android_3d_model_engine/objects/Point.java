package uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.objects;

import uz.ilhomjon.a3dxurshidakauchun.android_3d_model_engine.model.Object3DData;
import uz.ilhomjon.a3dxurshidakauchun.util.io.IOUtils;

import android.opengl.GLES20;

public final class Point {

    public static Object3DData build(float[] location) {
        float[] point = new float[]{location[0], location[1], location[2]};
        return new Object3DData(IOUtils.createFloatBuffer(point.length).put(point))
                .setDrawMode(GLES20.GL_POINTS).setId("Point");
    }
}
