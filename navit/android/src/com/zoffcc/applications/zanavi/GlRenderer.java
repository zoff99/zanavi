package com.zoffcc.applications.zanavi;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GlRenderer implements Renderer
{

	@Override
	public void onDrawFrame(GL10 gl)
	{
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
	}

	public static int loadShader(int type, String shaderCode)
	{

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	public static void checkGlError(String op)
	{
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
		{
			System.out.println("opengl:" + op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}
}
