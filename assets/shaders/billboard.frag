//
//  Shader.fsh
//  pathogen
//
//  Created by Denis Ivanov on 2012-12-14.
//  Copyright (c) 2012 Denis Ivanov. All rights reserved.
//

uniform lowp vec4 Color;

varying lowp vec2 TexCoordOut;
uniform sampler2D Texture;

void main(void)
{
	//lowp vec2 TexCoord = vec2(0,0);
    gl_FragColor = Color * texture2D(Texture, TexCoordOut);
    //gl_FragColor = vec4(0.5, 0.5, 0.5, 1.0) * texture2D(Texture, TexCoord);
    //gl_FragColor = vec4(1,1,1,1);
}
/*
uniform lowp vec4 Color;

varying lowp vec2 TexCoordOut;
uniform sampler2D Texture;

void main(void)
{
    //gl_FragColor = Color * texture2D(Texture, TexCoordOut);
    gl_FragColor = vec4(0,1,0,1);
}*/