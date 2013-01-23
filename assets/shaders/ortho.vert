//
//  Shader.vsh
//  pathogen
//
//  Created by Denis Ivanov on 2012-12-14.
//  Copyright (c) 2012 Denis Ivanov. All rights reserved.
//

attribute vec4 Position;

uniform float Width;
uniform float Height;

attribute vec2 TexCoordIn;
varying vec2 TexCoordOut;

void main(void)
{
    gl_Position = vec4(Position.x * 2.0 / Width - 1.0,
                     Position.y * -2.0 / Height + 1.0,
                     Position.z, 
                     1.0);
                     
    TexCoordOut = TexCoordIn;
}