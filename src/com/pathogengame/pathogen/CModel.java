package com.pathogengame.pathogen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.res.AssetManager;
import android.opengl.GLES20;

public class CModel 
{
	boolean on;
    md2_header_t header;
    
    md2_skin_t skins[];
    md2_texCoord_t texcoords[];
    md2_triangle_t triangles[];
    md2_frame_t frames[];
    //int *glcmds;
    
    int tex_id;
    
    int vertexBuffers[];
    CVertexArray vertexArrays[];
    int numverts;
    String name;
    
    boolean transp;
    
    MainActivity mActivity;
    
    CModel(MainActivity act)
    {
    	mActivity = act;
        on = false;
        transp = false;
    }

    void Load(String n, CVector3 scale)
    {
        String raw = CFile.StripPathExtension(n);
        String fullpath = "models/" + raw + ".md2";
        
        //boolean dothis = false;
        
        //if(raw.equalsIgnoreCase("human2lower"))
        //	dothis = true;
        
        System.out.println("--------------------");
        
        System.out.println("" + raw + "md2...");
        
        InputStream iS = CFile.GetInput(fullpath, mActivity);
        byte bucket[] = CFile.ReadWhole(iS);
        
        System.out.println("Read " + bucket.length + " bytes");
        
        name = raw;
        
        transp = false;
        for(int i=0; i<raw.length(); i++)
        {
            if(raw.charAt(i) == '%')
                transp = true;
        }

        /*
    int ident;
    int version;
    
    int skinwidth;
    int skinheight;
    
    int framesize;
    
    int num_skins;
    int num_vertices;
    int num_st;
    int num_tris;
    int num_glcmds;
    int num_frames;
    
    int offset_skins;
    int offset_st;
    int offset_tris;
    int offset_frames;
    int offset_glcmds;
    int offset_end;
         */
        
        int offset = 0;
        header = new md2_header_t();

        // Read header
        header.ident = CFile.ReadInt(bucket, offset + 0 * 4);
        header.version = CFile.ReadInt(bucket, offset + 1 * 4);
        header.skinwidth = CFile.ReadInt(bucket, offset + 2 * 4);
        header.skinheight = CFile.ReadInt(bucket, offset + 3 * 4);
        header.framesize = CFile.ReadInt(bucket, offset + 4 * 4);
        header.num_skins = CFile.ReadInt(bucket, offset + 5 * 4);
        header.num_vertices = CFile.ReadInt(bucket, offset + 6 * 4);
        header.num_st = CFile.ReadInt(bucket, offset + 7 * 4);
        header.num_tris = CFile.ReadInt(bucket, offset + 8 * 4);
        header.num_glcmds = CFile.ReadInt(bucket, offset + 9 * 4);
        header.num_frames = CFile.ReadInt(bucket, offset + 10 * 4);
        header.offset_skins = CFile.ReadInt(bucket, offset + 11 * 4);
        header.offset_st = CFile.ReadInt(bucket, offset + 12 * 4);
        header.offset_tris = CFile.ReadInt(bucket, offset + 13 * 4);
        header.offset_frames = CFile.ReadInt(bucket, offset + 14 * 4);
        header.offset_glcmds = CFile.ReadInt(bucket, offset + 15 * 4);
        header.offset_end = CFile.ReadInt(bucket, offset + 16 * 4);
        
        if ((header.ident != 844121161) ||
            (header.version != 8))
        {
            System.out.println("Bad version or identifier for model " + name);
            
            try 
            {
				iS.close();
			} 
            catch (IOException e) 
			{
				e.printStackTrace();
			}
            
            return;
        }
        
        // Memory allocations
        skins = new md2_skin_t[ header.num_skins ];
        texcoords = new md2_texCoord_t[ header.num_st ];
        triangles = new md2_triangle_t[ header.num_tris ];
        frames = new md2_frame_t[ header.num_frames ];
        //glcmds = new int[ header.num_glcmds ];
        
        // Read model data
        
    	//char name[64];
        int i, j;
        byte[] subbucket;
        for(i=0; i<header.num_skins; i++)
        {
        	skins[i] = new md2_skin_t();
        	
        	//if(dothis)
        	//	System.out.println("header.offset_skins = " + header.offset_skins);
        	
        	subbucket = CFile.SubBucket(bucket, header.offset_skins + 64*i, 64);
        	
        	String skinname = "";
        	for(j=0; j<64; j++)
        	{
        		if((char)subbucket[j] == '\0')
        			break;
        		
        		skinname += (char)subbucket[j];
        	}
        	
        	skins[i].name = skinname;
        	System.out.println("skin " + skinname);
        }
        
        /*
        short s;
        short t;
        */
        for(i=0; i<header.num_st; i++)
        {
        	texcoords[i] = new md2_texCoord_t();
        	// TO DO: byte packing?
        	texcoords[i].s = CFile.ReadShort(bucket, header.offset_st + (2+2)*i + 0);
        	texcoords[i].t = CFile.ReadShort(bucket, header.offset_st + (2+2)*i + 2);
        }
 
    	////unsigned short vertex[3];
    	////unsigned short st[3];
		//char vertex[] = new int[3];
		//char st[] = new int[3];
        
        for(i=0; i<header.num_tris; i++)
        {
        	triangles[i] = new md2_triangle_t();
        	// TO DO: byte packing?
        	triangles[i].vertex[0] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 0*2);
        	triangles[i].vertex[1] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 1*2);
        	triangles[i].vertex[2] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 2*2);
        	triangles[i].st[0] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 3*2);
        	triangles[i].st[1] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 4*2);
        	triangles[i].st[2] = CFile.ReadUShort(bucket, header.offset_tris + (2*3*2)*i + 5*2);
        }
        
        //fseek (fp, header.offset_glcmds, SEEK_SET);
        //fread (glcmds, sizeof (int), header.num_glcmds, fp);
        
        /*
        md2_frame_t 
        {
        	//vec3_t scale;
            //vec3_t translate;
            //char name[16];
            //struct md2_vertex_t *verts;
           
        md2_vertex_t 
		{
    		//unsigned char v[3];
    		//unsigned char normalIndex;
             */

        //if(dothis)
        {
	     //   System.out.println("header.num_frames = " + header.num_frames);
	     //   System.out.println("header.num_vertices = " + header.num_vertices);
	     //   System.out.println("header.num_tris = " + header.num_tris);
	    //    System.out.println("header.offset_frames = " + header.offset_frames);
        }
        
        // Read frames
        offset = header.offset_frames;
        for(i=0; i<header.num_frames; i++)
        {
        	//if(dothis)
        	//	System.out.println("frame " + i + " @ " + offset);
        	
        	frames[i] = new md2_frame_t();

        	frames[i].scale[0] = CFile.ReadFloat(bucket, offset + 4*0);
        	frames[i].scale[1] = CFile.ReadFloat(bucket, offset + 4*1);
        	frames[i].scale[2] = CFile.ReadFloat(bucket, offset + 4*2);
        	frames[i].translate[0] = CFile.ReadFloat(bucket, offset + 4*3);
        	frames[i].translate[1] = CFile.ReadFloat(bucket, offset + 4*4);
        	frames[i].translate[2] = CFile.ReadFloat(bucket, offset + 4*5);
        	
        	subbucket = CFile.SubBucket(bucket, offset + 4*6, 16);
        	String framename = "";
        	for(j=0; j<16; j++)
        	{
        		if((char)subbucket[j] == '\0')
        			break;
        		
        		framename += (char)subbucket[j];
        	}
        	frames[i].name = framename;
        	
        	offset += 4*6 + 16;
            frames[i].verts = new md2_vertex_t[ header.num_vertices ];
            
            for(j=0; j<header.num_vertices; j++)
            {
            	frames[i].verts[j] = new md2_vertex_t();

            	frames[i].verts[j].v[0] = CFile.ReadUByte(bucket, offset + 1*0);
            	frames[i].verts[j].v[1] = CFile.ReadUByte(bucket, offset + 1*1);
            	frames[i].verts[j].v[2] = CFile.ReadUByte(bucket, offset + 1*2);
            	frames[i].verts[j].normalIndex = CFile.ReadUByte(bucket, offset + 1*3);
            	offset += 1*4;
            }
            
            //if(dothis)
            //	System.out.println("done frame " + i);
        }
        
        try 
        {
			iS.close();
		} 
        catch (IOException e) 
		{
			e.printStackTrace();
		}
        //fclose (fp);
        on = true;
        
        String texn = CFile.StripPathExtension(skins[0].name);
        skins[0].name = "models/" + texn;
        tex_id = mActivity.CreateTexture(skins[0].name, true);
        
        if(mActivity.mLastTexTransp)
            transp = true;
        
        //vertexBuffers = new int[ header.num_frames ];
        numverts = header.num_tris * 3;
        
    	//md2_frame_t pframe;
    	//md2_vertex_t pvert;
    	int index;
        
    	
        //CVertexArray2 va2[] = new CVertexArray2[ numverts ];
    	FloatBuffer va2 = FloatBuffer.allocate( numverts * (3 + 2 + 3) );
        float temp;
        int f;
		CVector3 normal = new CVector3();
		CVector3 tri[] = new CVector3[3];
		tri[0] = new CVector3();
		tri[1] = new CVector3();
		tri[2] = new CVector3();
        
		/*
    	for(f=0; f<header.num_frames; f++)
    	{	
    		for (i = 0; i < header.num_tris; ++i)
    		{
    			for (j = 0; j < 3; ++j)
    			{
    				index = i*3 + j;
                    
    				pframe = frames[f];
    				pvert = pframe.verts[(int)triangles[i].vertex[j]];
                    
                     // Reverse vertex order
                     //if(j == 1)
                     //		index += 1;
                     //else if(j == 2)
                     //		index -= 1;
    				
    				//CVector3 vertex; [0 1 2]
    				//CVector2 texcoord; [3 4]
    				//CVector3 normal; [5 6 7]
                    
    				// Compute texture coordinates
    				//va2[index].texcoord.x = (float)texcoords[triangles[i].st[j]].s / header.skinwidth;
    				//va2[index].texcoord.y = (float)texcoords[triangles[i].st[j]].t / header.skinheight;
    				va2.put( index * (3+2+3) + 3, (float)texcoords[triangles[i].st[j]].s / header.skinwidth);
    				va2.put( index * (3+2+3) + 4, (float)texcoords[triangles[i].st[j]].t / header.skinheight);
                    
    				// Normal vector
    				//glNormal3fv (anorms_table[pvert->normalIndex]);
    				//va[index].normal.x = anorms_table[pvert->normalIndex][0];
    				//va[index].normal.y = anorms_table[pvert->normalIndex][2];
    				//va[index].normal.z = -anorms_table[pvert->normalIndex][1];
                    
    				// Calculate vertex position
    				//va2[index].vertex.x = (pframe.scale[0] * pvert.v[0]) + pframe.translate[0];
    				//va2[index].vertex.y = (pframe.scale[2] * pvert.v[2]) + pframe.translate[2];
    				//va2[index].vertex.z = -((pframe.scale[1] * pvert.v[1]) + pframe.translate[1]);
    				va2.put( index * (3+2+3) + 0, (pframe.scale[0] * pvert.v[0]) + pframe.translate[0]);
    				va2.put( index * (3+2+3) + 1, (pframe.scale[2] * pvert.v[2]) + pframe.translate[2]);
    				va2.put( index * (3+2+3) + 2, -((pframe.scale[1] * pvert.v[1]) + pframe.translate[1]));
                    
    				//va2[index].vertex.x *= scale.x;
    				//va2[index].vertex.y *= scale.y;
    				//va2[index].vertex.z *= scale.z;
    				va2.put( index * (3+2+3) + 0, va2.get(index * (3+2+3) + 0) * scale.x);
    				va2.put( index * (3+2+3) + 1, va2.get(index * (3+2+3) + 1) * scale.y);
    				va2.put( index * (3+2+3) + 2, va2.get(index * (3+2+3) + 2) * scale.z);
                    
    				//va2[index].vertex.x += translate.x;
    				//va2[index].vertex.y += translate.y;
    				//va2[index].vertex.z += translate.z;
                    
                    //Flip 90 degrees
    				//temp = va2[index].vertex.x;
    				//va2[index].vertex.x = -va2[index].vertex.z;
    				//va2[index].vertex.z = temp;
    				temp = va2.get(index * (3+2+3) + 0);
    				va2.put( index * (3+2+3) + 0, -va2.get(index * (3+2+3) + 2));
    				va2.put( index * (3+2+3) + 2, temp);
    			}
    		}
			
            for(i=0; i<numverts; i+=3)
    		{
    			//tri[0] = va2[i].vertex;
    			//tri[1] = va2[i+1].vertex;
    			//tri[2] = va2[i+2].vertex;
            	tri[0].x = va2.get((i+0) * (3+2+3) + 0);
            	tri[0].y = va2.get((i+0) * (3+2+3) + 1);
            	tri[0].z = va2.get((i+0) * (3+2+3) + 2);
            	tri[1].x = va2.get((i+1) * (3+2+3) + 0);
            	tri[1].y = va2.get((i+1) * (3+2+3) + 1);
            	tri[1].z = va2.get((i+1) * (3+2+3) + 2);
            	tri[2].x = va2.get((i+2) * (3+2+3) + 0);
            	tri[2].y = va2.get((i+2) * (3+2+3) + 1);
            	tri[2].z = va2.get((i+2) * (3+2+3) + 2);
    			normal = Math3D.Normal(tri);
    			//va2[i].normal = Math3D.Copy(normal);
    			//va2[i+1].normal = Math3D.Copy(normal);
    			//va2[i+2].normal = Math3D.Copy(normal);
    			va2.put((i+0) * (3+2+3) + 5, normal.x);
    			va2.put((i+0) * (3+2+3) + 6, normal.y);
    			va2.put((i+0) * (3+2+3) + 7, normal.z);
    			va2.put((i+1) * (3+2+3) + 5, normal.x);
    			va2.put((i+1) * (3+2+3) + 6, normal.y);
    			va2.put((i+1) * (3+2+3) + 7, normal.z);
    			va2.put((i+2) * (3+2+3) + 5, normal.x);
    			va2.put((i+2) * (3+2+3) + 6, normal.y);
    			va2.put((i+2) * (3+2+3) + 7, normal.z);
    		}

            va2.rewind();
            GLES20.glGenBuffers(1, vertexBuffers, f);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[f]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, (3*4*2 + 2*4) * numverts, va2, GLES20.GL_STATIC_DRAW);
    	}
        
    	va2 = null;
    	System.gc();
        //delete [] va2;
        */
		
		/*
		System.out.println("va header.num_frames="+header.num_frames+" numverts="+numverts);
        CVertexArray va[] = new CVertexArray[ header.num_frames ];
        
    	for(f=0; f<header.num_frames; f++)
    	{
    		va[f] = new CVertexArray();
    		
    		va[f].numverts = numverts;
    		va[f].vertices = new CVector3[ numverts ];
    		va[f].texcoords = new CVector2[ numverts ];
    		va[f].normals = new CVector3[ numverts ];
            
    		for (i = 0; i < header.num_tris; ++i)
    		{
    			for (j = 0; j < 3; ++j)
    			{
    				index = i*3 + j;
                    
    				pframe = frames[f];
    				pvert = pframe.verts[triangles[i].vertex[j]];
                    
    				// Reverse vertex order
    				if(j == 1)
    					index += 1;
    				else if(j == 2)
    					index -= 1;
    				
    				va[f].vertices[index] = new CVector3();
    				va[f].texcoords[index] = new CVector2();
    				va[f].normals[index] = new CVector3();
                    
    				// Compute texture coordinates
    				va[f].texcoords[index].x = (float)texcoords[triangles[i].st[j]].s / header.skinwidth;
    				va[f].texcoords[index].y = (float)texcoords[triangles[i].st[j]].t / header.skinheight;
                    
    				// Normal vector
    				//glNormal3fv (anorms_table[pvert->normalIndex]);
    				//va[index].normal.x = anorms_table[pvert->normalIndex][0];
    				//va[index].normal.y = anorms_table[pvert->normalIndex][2];
    				//va[index].normal.z = -anorms_table[pvert->normalIndex][1];
                    
    				// Calculate vertex position
    				va[f].vertices[index].x = (pframe.scale[0] * pvert.v[0]) + pframe.translate[0];
    				va[f].vertices[index].y = (pframe.scale[2] * pvert.v[2]) + pframe.translate[2];
    				va[f].vertices[index].z = -((pframe.scale[1] * pvert.v[1]) + pframe.translate[1]);
                    
    				va[f].vertices[index].x *= scale.x;
    				va[f].vertices[index].y *= scale.y;
    				va[f].vertices[index].z *= scale.z;
                    
    				//va[f].vertices[index].x += translate.x;
    				//va[f].vertices[index].y += translate.y;
    				//va[f].vertices[index].z += translate.z;
                    
                    //Flip 90 degrees
    				temp = va[f].vertices[index].x;
    				va[f].vertices[index].x = -va[f].vertices[index].z;
    				va[f].vertices[index].z = temp;
    				
    			}
    		}
            
            for(i=0; i<numverts; i+=3)
    		{
    			//CVector3 normal;
    			//CVector3 tri[] = new CVector3[3];
    			//tri[0] = new CVector3();
    			//tri[1] = new CVector3();
    			//tri[2] = new CVector3();
    			tri[0] = va[f].vertices[i];
    			tri[1] = va[f].vertices[i+1];
    			tri[2] = va[f].vertices[i+2];
    			normal = Math3D.Normal2(tri);
    			va[f].normals[i] = normal;
    			va[f].normals[i+1] = normal;
    			va[f].normals[i+2] = normal;
    		}
    	}
        
        vertexArrays = va;
        System.gc();
        */
        
        System.out.println(n + ".md2");
    }

    //void Free();
    //void DrawSky(int frame, CVector3 pos);
    void Draw(int frame, CVector3 pos, float pitch, float yaw)
    {
        //if(!on)
        //{
            //NSLog(@"draw null %@", name);
            //return;
        //}
        
        CMatrix modelmat = new CMatrix();
        float radians[] = {pitch, yaw, 0};
        modelmat.setTranslation(pos);
        CMatrix rotation = new CMatrix();
        rotation.setRotationRadians(radians);
        modelmat.postMultiply(rotation);
        
        CShader s = mActivity.mShader[CShader.MODEL];
        //FloatBuffer modelmatfb = FloatBuffer.allocateDirect(16);
        //modelmatfb.put(modelmat.getMatrix());
        GLES20.glUniformMatrix4fv(s.slot[CShader.MODELMAT], 1, false, modelmat.getMatrix(), 0);
        
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[frame]);
        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, (3+2+3)*4, 0*4);
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, (3+2+3)*4, 3*4);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_id);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numverts);
    }
    
	//used for drawing the upper part of characters
    void Draw2(int frame, CVector3 pos, float pitch, float yaw)
    {
        CVector3 offset = Math3D.Add(pos, new CVector3(0, CEntity.MID_HEIGHT_OFFSET, 0));
        CMatrix modelmat = new CMatrix();
        float radians[] = {pitch, yaw, 0};
        modelmat.setTranslation(offset);
        CMatrix rotation = new CMatrix();
        rotation.setRotationRadians(radians);
        modelmat.postMultiply(rotation);
        CMatrix translation = new CMatrix();
        offset = new CVector3(0, -CEntity.MID_HEIGHT_OFFSET, 0);
        translation.setTranslation(offset);
        modelmat.postMultiply(translation);

        CShader s = mActivity.mShader[CShader.MODEL];
        //FloatBuffer modelmatfb = FloatBuffer.allocateDirect(16);
        //modelmatfb.put(modelmat.getMatrix());
        GLES20.glUniformMatrix4fv(s.slot[CShader.MODELMAT], 1, false, modelmat.getMatrix(), 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[frame]);
        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, (3+2+3)*4, 0*4);
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, (3+2+3)*4, 3*4);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_id);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numverts);
    }
    
    static boolean PlayAnimation(CFloat frame, int first, int last, boolean loop, float rate)
    {
        if(frame.value < first || frame.value > last)
        {
            frame.value = first;
            return false;
        }
        
        frame.value += rate;
        
        if(frame.value > last)
        {
            if(loop)
                frame.value = first;
    		else
    			frame.value = last;
            
            return true;
        }
        
        return false;
    }
    
    //Play animation backwards
    static boolean PlayAnimationB(CFloat frame, int first, int last, boolean loop, float rate)
    {
        if(frame.value < first || frame.value > last)
        {
            frame.value = last;
            return false;
        }
        
        frame.value -= rate;
        
        if(frame.value < first)
        {
            if(loop)
                frame.value = last;
    		else
    			frame.value = first;
            
            return true;
        }
        
        return false;
    }
    
    void Free()
    {
    	IntBuffer ib = IntBuffer.allocate(header.num_frames);
    	ib.put(vertexBuffers);
        GLES20.glDeleteBuffers(header.num_frames, ib);
        ib = IntBuffer.allocate(1);
        ib.put(tex_id);
        GLES20.glDeleteTextures(1, ib);
        
    	for(int i=0; i<header.num_frames; i++)
    		vertexArrays[i].free();
        
        if(skins != null)
        {
            skins = null;
        }
        
        if(texcoords != null)
        {
            texcoords = null;
        }

        if(triangles != null)
        {
            triangles = null;
        }
        
        //if(glcmds != null)
        //{
        //    delete [] glcmds;
        //    glcmds = null;
       // }
        
        if(frames != null)
        {
            for (int i=0; i<header.num_frames; i++)
            {
                frames[i].verts = null;
            }
            
            frames = null;
        }
    }
}
