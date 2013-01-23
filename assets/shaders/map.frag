//
//  Shader.fsh
//  pathogen
//
//  Created by Denis Ivanov on 2012-12-14.
//  Copyright (c) 2012 Denis Ivanov. All rights reserved.
//

uniform lowp vec4 Color;

varying lowp vec2 TexCoordOut;
varying lowp vec2 TexCoordOut2;
uniform sampler2D Texture;
uniform sampler2D Texture2;

void main(void)
{
    gl_FragColor = Color * texture2D(Texture, TexCoordOut) * texture2D(Texture2, TexCoordOut2);
    //gl_FragColor = Color * texture2D(Texture2, TexCoordOut2);
}