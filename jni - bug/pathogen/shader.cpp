
#include "shader.h"
#include "logger.h"
#include "main.h"
#include "file.h"

unsigned int g_slots[SHADERS][SLOTS];
unsigned int g_program[SHADERS];

string LoadTextFile(const char* strFile)
{
	CFile file(strFile);

	if(!file.mFile)
		return "";

	string strText = "";

	char buf[256];

	int r;
	while((r = file.read((void*)buf, 255)) > 0)
	{
		buf[r] = '\0';
		strText += buf;
	}

	return strText;
}

bool CompileShader(unsigned int* shader, GLenum type, const char* file)
{
    GLint status;
    string source;
    
    source = LoadTextFile(file);
	const char* szShader = source.c_str();

    if (!szShader || !szShader[0])
    {
        LOGE("Failed to load shader %s", file);
        return false;
    }
    
    *shader = glCreateShader(type);
    glShaderSource(*shader, 1, &szShader, NULL);
    glCompileShader(*shader);

    int logLength;
    glGetShaderiv(*shader, GL_INFO_LOG_LENGTH, &logLength);
    if (logLength > 0)
    {
        char *log = (char *)malloc(logLength);
        glGetShaderInfoLog(*shader, logLength, &logLength, log);
        LOGI("Shader compile log:\n%s", log);
        free(log);
    }
    
    glGetShaderiv(*shader, GL_COMPILE_STATUS, &status);
    if (status == 0)
    {
        glDeleteShader(*shader);
        return false;
    }
    
    return true;
}

bool LinkProgram(unsigned int prog)
{
    int status;
    glLinkProgram(prog);
    
    GLint logLength;
    glGetProgramiv(prog, GL_INFO_LOG_LENGTH, &logLength);
    if (logLength > 0)
    {
        GLchar *log = (GLchar *)malloc(logLength);
        glGetProgramInfoLog(prog, logLength, &logLength, log);
        LOGI("Program link log:\n%s", log);
        free(log);
    }
    
    glGetProgramiv(prog, GL_LINK_STATUS, &status);
    if (status == 0)
        return false;
    
    return true;
}

bool LoadShader(unsigned int* program, unsigned int* slots, const char* vert, const char* frag)
{
    unsigned int vertShader, fragShader;
    
    // Create shader program.
    (*program) = glCreateProgram();
    
    // Create and compile vertex shader.
    if (!CompileShader(&vertShader, GL_VERTEX_SHADER, vert))
    {
        LOGE("Failed to compile vertex shader %s", vert);
        return false;
    }
    
    // Create and compile fragment shader.
    if (!CompileShader(&fragShader, GL_FRAGMENT_SHADER, frag))
    {
        LOGE("Failed to compile fragment shader %s", frag);
        return false;
    }
    
    // Attach vertex shader to program.
    glAttachShader(*program, vertShader);
    
    // Attach fragment shader to program.
    glAttachShader(*program, fragShader);
    
    // Bind attribute locations.
    // This needs to be done prior to linking.
    
    // Link program.
    if (!LinkProgram(*program))
    {
        LOGE("Failed to link program: %d", *program);
        
        if (vertShader)
        {
            glDeleteShader(vertShader);
            vertShader = 0;
        }
        if (fragShader)
        {
            glDeleteShader(fragShader);
            fragShader = 0;
        }
        if (*program)
        {
            glDeleteProgram(*program);
            (*program) = 0;
        }
        
        return false;
    }
    
    // Get attribute locations.
    slots[POSITION] = glGetAttribLocation(*program, "Position");
    slots[TEXCOORD] = glGetAttribLocation(*program, "TexCoordIn");

	LOGI("%s%s slots[POSITION]=%u", vert, frag, slots[POSITION]);
	LOGI("%s%s slots[TEXCOORD]=%u", vert, frag, slots[TEXCOORD]);

    // Get uniform locations.
    slots[PROJECTION] = glGetUniformLocation(*program, "Projection");
    slots[MODELMAT] = glGetUniformLocation(*program, "Model");
    slots[VIEWMAT] = glGetUniformLocation(*program, "View");
    slots[TEXTURE] = glGetUniformLocation(*program, "Texture");
    slots[WIDTH] = glGetUniformLocation(*program, "Width");
    slots[HEIGHT] = glGetUniformLocation(*program, "Height");

	LOGI("%s%s slots[TEXTURE]=%u", vert, frag, slots[TEXTURE]);
	LOGI("%s%s slots[WIDTH]=%u", vert, frag, slots[WIDTH]);
	LOGI("%s%s slots[HEIGHT]=%u", vert, frag, slots[HEIGHT]);
    
    // Release vertex and fragment shaders.
    if (vertShader)
    {
        glDetachShader(*program, vertShader);
        glDeleteShader(vertShader);
    }
    if (fragShader)
    {
        glDetachShader(*program, fragShader);
        glDeleteShader(fragShader);
    }

	LOGI("%s %s %u", vert, frag, (*program));
    
    return true;
}


void LoadShaders()
{	
    LoadShader(&g_program[ORTHO], g_slots[ORTHO], "shaders/ortho.vert", "shaders/ortho.frag");
    LoadShader(&g_program[MODEL], g_slots[MODEL], "shaders/model.vert", "shaders/model.frag");
}