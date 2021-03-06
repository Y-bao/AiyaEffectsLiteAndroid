package com.aiyaapp.camera.sdk.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.TextureView;

import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ActionObserver;
import com.aiyaapp.camera.sdk.base.Event;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/6/26.
 */

public class AnimEffectTextureView extends TextureView implements TextureView.SurfaceTextureListener,
        GLSurfaceView.Renderer, ActionObserver {

    private GLEnvironment mEnv;
    private SurfaceTexture mTexture;
    private int width, height;
    private float[] SM = new float[16];                       //用于绘制到屏幕上的变换矩阵

    private int[] tempTextures = new int[1];

    private AnimEndListener mAnimEndListener;
    private AnimListener mFrameListener;
    private GLEnvironment.ErrorListener mErrorListener;

    public AnimEffectTextureView(Context context) {
        this(context, null);
    }

    public AnimEffectTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimEffectTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMode(ISdkManager.MODE_GIFT);
        setOpaque(false);
        setKeepScreenOn(true);
        animate().scaleY(-1).withLayer();
        setSurfaceTextureListener(this);

        mEnv = new GLEnvironment(getContext());
        mEnv.setEGLContextClientVersion(2);
        mEnv.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mEnv.setPreserveEGLContextOnPause(false);
        mEnv.setEGLWindowSurfaceFactory(new GLEnvironment.EGLWindowSurfaceFactory() {
            @Override
            public EGLSurface createSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
                return egl.eglCreateWindowSurface(display, config, mTexture, null);
            }

            @Override
            public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                egl.eglDestroySurface(display, surface);
            }
        });
        mEnv.setPreserveEGLContextOnPause(true);
        mEnv.setRenderer(this);
        mEnv.setRenderMode(GLEnvironment.RENDERMODE_WHEN_DIRTY);
    }

    public void setEffect(String effect) {
        if (effect == null) {
            AiyaEffects.getInstance().setEffect(null);
            mEnv.setRenderMode(GLEnvironment.RENDERMODE_WHEN_DIRTY);
            mEnv.requestRender();
        } else {
            mEnv.setRenderMode(GLEnvironment.RENDERMODE_CONTINUOUSLY);
            AiyaEffects.getInstance().setEffect(effect);
            if (mFrameListener != null) {
                mFrameListener.onAnimStart(effect);
            }
        }
    }

    public boolean isAnimEffectPlaying() {
        return !AiyaEffects.getInstance().isEffectNull();
    }

    public void setAnimEndListener(AnimEndListener listener) {
        this.mAnimEndListener = listener;
    }

    public void setAnimListener(AnimListener listener) {
        this.mFrameListener = listener;
    }

    public void setMode(int mode) {
        AiyaEffects.getInstance().set(ISdkManager.SET_MODE, mode);
    }

    public void setOnErrorListener(GLEnvironment.ErrorListener listener) {
        this.mErrorListener = listener;
        mEnv.setOnErrorListener(listener);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mTexture = surface;
        Log.d("AnimE", "onSurfaceTextureAvailable");
        mEnv.onAttachedToWindow();
        mEnv.surfaceCreated(null);
        mEnv.surfaceChanged(null, 0, width, height);
        //if (isAnimEffectPlaying()){
        //    mEnv.onPause();
        //}
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("AnimE", "onSurfaceTextureSizeChanged");
//        mEnv.surfaceChanged(null, 0, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("AnimE", "onSurfaceTextureDestroyed");
        this.mTexture = null;
        this.width=0;
        this.height=0;
        mEnv.surfaceDestroyed(null);
        mEnv.onDetachedFromWindow();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_FORCE_CLOSE, 1);
        GLES20.glGenTextures(1, tempTextures, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        AiyaEffects.getInstance().set(ISdkManager.SET_IN_WIDTH, width);
        AiyaEffects.getInstance().set(ISdkManager.SET_IN_HEIGHT, height);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_WIDTH, width);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_HEIGHT, height);
        if (width > height && width > 320) {
            if (width > 320) {
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH, 320);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT, 320 * height / width);
            }
        } else if (height > width && height > 320) {
            if (height > 320) {
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH, 320 * width / height);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT, 320);
            }
        } else {
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH, width);
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT, height);
        }

        AiyaEffects.getInstance().set(ISdkManager.SET_ACTION, ISdkManager.ACTION_REFRESH_PARAMS_NOW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);
        //AiyaEffects.getInstance().track(null,null,0);
        AiyaEffects.getInstance().process(-1, 0);
        if (mFrameListener != null && isAnimEffectPlaying()) {
            mFrameListener.onFrame();
        }
    }

    public void onResume() {
        Log.d("AnimE","onResume");
        if(mEnv!=null){
            if(mTexture!=null){
                onSurfaceTextureAvailable(mTexture,width,height);
            }
            mEnv.onAttachedToWindow();
        }
        AiyaEffects.getInstance().registerObserver(this);
    }

    public void onPause() {
        Log.d("AnimE","onResume");
        if(mEnv!=null){
            mEnv.surfaceDestroyed(null);
            mEnv.onDetachedFromWindow();
        }
        AiyaEffects.getInstance().unRegisterObserver(this);
    }

    @Override
    public void onAction(Event event) {
        switch (event.eventType) {
            case Event.PROCESS_END:
                mEnv.setRenderMode(GLEnvironment.RENDERMODE_WHEN_DIRTY);
                mEnv.requestRender();
                if (mFrameListener != null) {
                    mFrameListener.onAnimEnd(event.strTag);
                }
                if (mAnimEndListener != null) {
                    mAnimEndListener.onAnimEnd(event.strTag);
                }
                break;
            case Event.PROCESS_ERROR:
                if (mFrameListener != null) {
                    mFrameListener.onAnimError();
                }
                if (mErrorListener != null) {
                    mErrorListener.onError(event.intTag, event.strTag);
                }
                break;
        }
    }

    public interface AnimEndListener {
        void onAnimEnd(String effect);
    }

    public interface AnimListener extends AnimEndListener {
        void onAnimStart(String effect);

        void onAnimError();

        void onFrame();
    }

}
