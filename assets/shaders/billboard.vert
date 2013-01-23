//
//  Shader.vsh
//  pathogen
//
//  Created by Denis Ivanov on 2012-12-14.
//  Copyright (c) 2012 Denis Ivanov. All rights reserved.
//

attribute vec4 Position;

//uniform vec3 CameraPos;
//uniform float Scale;

uniform mat4 Projection;
uniform mat4 Model;
uniform mat4 View;

attribute vec2 TexCoordIn;
varying vec2 TexCoordOut;

void main(void)
{
    gl_Position = Projection * (View * (Model * Position));
    TexCoordOut = TexCoordIn;
}