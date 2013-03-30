
#include "shader.h"
#include "logger.h"
#include "main.h"
#include "file.h"

unsigned int g_slots[SHADERS][SLOTS];
unsigned int g_program[SHADERS];

string LoadTextFile(const char* strFile)
{
	//LOGI("Reading %s...", strFile);

	//int err = 0;
	//ifstream fin(strFile);
	//zip_file* file = zip_fopen(APKArchive, strFile, 0);
	CFile file(strFile);

	//LOGI("Opened");

	//if(!fin)
	//if(!file)
	//if(file.fsize <= 0)
	if(!file.mFile)
		return "";

	//string strLine = "";
	string strText = "";

	char buf[256];
	//LOGI("fread...1");

	int r=0;
	//while((r = zip_fread(file, buf, 128)) > 0)
	//while((r=zip_fread(file, buf, 255)) > 0)
	while((r = file.read((void*)buf, 255)) > 0)
	{
		buf[r] = '\0';
		//LOGI("%d ", r);
		//LOGI("--------------------------\n\r%s", buf);
		strText += buf;
	}

	//strText += file.data += '\0';

	//LOGI("%s", strText.c_str());

	//while(getline(fin, strLine))
		//strText = strText + "\n" + strLine;

	//fin.close();

	//zip_fclose(file);

	return strText;
}

bool CompileShader(unsigned int* shader, GLenum type, const char* file)
{
    GLint status;
    string source;
    
    source = LoadTextFile(file);
	const char* szShader = source.c_str();

	//return false;

	//LOGI("%s", szShader);

    if (!szShader || !szShader[0])
    {
        LOGE("Failed to load shader %s", file);
        return false;
    }
    
    *shader = glCreateShader(type);
    glShaderSource(*shader, 1, &szShader, NULL);
    glCompileShader(*shader);

	//return false;
    
//#define DEBUG
//#if defined(DEBUG)
    int logLength;
    glGetShaderiv(*shader, GL_INFO_LOG_LENGTH, &logLength);
    if (logLength > 0)
    {
        char *log = (char *)malloc(logLength);
        glGetShaderInfoLog(*shader, logLength, &logLength, log);
        LOGI("Shader compile log:\n%s", log);
        free(log);
    }
	//else
	//	LOGI("No shader compile log");
//#endif
    
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
    
//#define DEBUG
//#if defined(DEBUG)
    GLint logLength;
    glGetProgramiv(prog, GL_INFO_LOG_LENGTH, &logLength);
    if (logLength > 0)
    {
        GLchar *log = (GLchar *)malloc(logLength);
        glGetProgramInfoLog(prog, logLength, &logLength, log);
        LOGI("Program link log:\n%s", log);
        free(log);
    }
//#endif
    
    glGetProgramiv(prog, GL_LINK_STATUS, &status);
    if (status == 0)
        return false;
    
    return true;
}

bool LoadShader(unsigned int* program, unsigned int* slots, const char* vert, const char* frag)
{
    unsigned int vertShader, fragShader;
    //NSString *vertShaderPathname, *fragShaderPathname;
    
    // Create shader program.
    (*program) = glCreateProgram();
    
    // Create and compile vertex shader.
    //vertShaderPathname = [[NSBundle mainBundle] pathForResource:vert ofType:@"vert"];
    if (!CompileShader(&vertShader, GL_VERTEX_SHADER, vert))
    {
        LOGE("Failed to compile vertex shader %s", vert);
        return false;
    }
    
    // Create and compile fragment shader.
    //fragShaderPathname = [[NSBundle mainBundle] pathForResource:frag ofType:@"frag"];
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
    
    // Get uniform locations.
    slots[POSITION] = glGetAttribLocation(*program, "Position");
    slots[TEXCOORD] = glGetAttribLocation(*program, "TexCoordIn");
    slots[TEXCOORD2] = glGetAttribLocation(*program, "TexCoordIn2");
    
    slots[PROJECTION] = glGetUniformLocation(*program, "Projection");
    slots[MODELMAT] = glGetUniformLocation(*program, "Model");
    slots[VIEWMAT] = glGetUniformLocation(*program, "View");
    slots[TEXTURE] = glGetUniformLocation(*program, "Texture");
    slots[TEXTURE2] = glGetUniformLocation(*program, "Texture2");
    slots[COLOR] = glGetUniformLocation(*program, "Color");
    slots[WIDTH] = glGetUniformLocation(*program, "Width");
    slots[HEIGHT] = glGetUniformLocation(*program, "Height");
    slots[CAMERAPOS] = glGetUniformLocation(*program, "CameraPos");
    slots[SCALE] = glGetUniformLocation(*program, "Scale");
    
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

	LOGI("%s %s", vert, frag);
    
    return true;
}


void LoadShaders()
{	
    LoadShader(&g_program[SKY], g_slots[SKY], "shaders/sky.vert", "shaders/sky.frag");
    LoadShader(&g_program[MAP], g_slots[MAP], "shaders/map.vert", "shaders/map.frag");
    LoadShader(&g_program[MODEL], g_slots[MODEL], "shaders/model.vert", "shaders/model.frag");
    LoadShader(&g_program[ORTHO], g_slots[ORTHO], "shaders/ortho.vert", "shaders/ortho.frag");
    LoadShader(&g_program[BILLBOARD], g_slots[BILLBOARD], "shaders/billboard.vert", "shaders/billboard.frag");
}