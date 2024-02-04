package io.github.aratakileo.emogg.util;

import com.mojang.blaze3d.shaders.AbstractUniform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.function.Consumer;

public class MultiUniform extends AbstractUniform {
    private static final AbstractUniform DUMMY = new AbstractUniform();

    public final AbstractUniform[] uniforms;

    public MultiUniform(int numUniforms) {
        super();
        this.uniforms = new AbstractUniform[numUniforms];
        Arrays.fill(this.uniforms, DUMMY);
    }
    
    private void apply(Consumer<AbstractUniform> operation) {
        for (AbstractUniform uniform : uniforms) {
            operation.accept(uniform);
        }
    }

    @Override
    public void set(float f) {
        apply(u -> u.set(f));
    }

    @Override
    public void set(float f, float g) {
        apply(u -> u.set(f, g));
    }

    @Override
    public void set(float f, float g, float h) {
        apply(u -> u.set(f, g, h));
    }

    @Override
    public void set(float f, float g, float h, float i) {
        apply(u -> u.set(f, g, h, i));
    }

    @Override
    public void setSafe(float f, float g, float h, float i) {
        apply(u -> u.setSafe(f, g, h, i));
    }

    @Override
    public void setSafe(int i, int j, int k, int l) {
        apply(u -> u.setSafe(i, j, k, l));
    }

    @Override
    public void set(int i) {
        apply(u -> u.set(i));
    }

    @Override
    public void set(int i, int j) {
        apply(u -> u.set(i, j));
    }

    @Override
    public void set(int i, int j, int k) {
        apply(u -> u.set(i, j, k));
    }

    @Override
    public void set(int i, int j, int k, int l) {
        apply(u -> u.set(i, j, k, l));
    }

    @Override
    public void set(float[] fs) {
        apply(u -> u.set(fs));
    }

    @Override
    public void set(Vector3f vector3f) {
        apply(u -> u.set(vector3f));
    }

    @Override
    public void set(Vector4f vector4f) {
        apply(u -> u.set(vector4f));
    }

    @Override
    public void setMat2x2(float f, float g, float h, float i) {
        apply(u -> u.setMat2x2(f, g, h, i));
    }

    @Override
    public void setMat2x3(float f, float g, float h, float i, float j, float k) {
        apply(u -> u.setMat2x3(f, g, h, i, j, k));
    }

    @Override
    public void setMat2x4(float f, float g, float h, float i, float j, float k, float l, float m) {
        apply(u -> u.setMat2x4(f, g, h, i, j, k, l, m));
    }

    @Override
    public void setMat3x2(float f, float g, float h, float i, float j, float k) {
        apply(u -> u.setMat3x2(f, g, h, i, j, k));
    }

    @Override
    public void setMat3x3(float f, float g, float h, float i, float j, float k, float l, float m, float n) {
        apply(u -> u.setMat3x3(f, g, h, i, j, k, l, m, n));
    }

    @Override
    public void setMat3x4(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
        apply(u -> u.setMat3x4(f, g, h, i, j, k, l, m, n, o, p, q));
    }

    @Override
    public void setMat4x2(float f, float g, float h, float i, float j, float k, float l, float m) {
        apply(u -> u.setMat4x2(f, g, h, i, j, k, l, m));
    }

    @Override
    public void setMat4x3(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
        apply(u -> u.setMat4x3(f, g, h, i, j, k, l, m, n, o, p, q));
    }

    @Override
    public void setMat4x4(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u) {
        apply(un -> un.setMat4x4(f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u));
    }

    @Override
    public void set(Matrix4f matrix4f) {
        apply(u -> u.set(matrix4f));
    }

    @Override
    public void set(Matrix3f matrix3f) {
        apply(u -> u.set(matrix3f));
    }
}
