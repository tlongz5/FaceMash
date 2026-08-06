package com.example.facemash.utils.openGL

import com.otaliastudios.cameraview.filter.BaseFilter

class SkinFilter: BaseFilter() {
    // camera designed theo chieu ngang va mirror
    override fun getFragmentShader(): String {
        return """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            
            varying vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;
            
            void main() {
                vec4 cameraColor = texture2D(sTexture, vTextureCoord);
                
                if(vTextureCoord.y < 0.5) {
                    vec3 rgb = cameraColor.rgb;
                    
                    // swap rgb to ycbcr
                    
                    float Y  = 0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b;
                    float Cb = -0.169 * rgb.r - 0.331 * rgb.g + 0.5 * rgb.b + 0.5;
                    float Cr = 0.5 * rgb.r - 0.419 * rgb.g - 0.081 * rgb.b + 0.5;
                    
                    bool isSkin = (Cb >= 0.30 && Cb <= 0.50 && Cr >= 0.52 && Cr <= 0.68);
                    
                    if(isSkin) {
                        gl_FragColor = mix(cameraColor, vec4(0.0, 1.0, 0.0, 1.0),0.1); 
                    } else {
                        gl_FragColor = cameraColor;
                    }
                } else {
                    gl_FragColor = cameraColor;
                }
            }
        """.trimIndent()
    }
}