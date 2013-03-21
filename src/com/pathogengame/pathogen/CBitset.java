package com.pathogengame.pathogen;

public class CBitset
{   
	//unsigned int m_bits[];
	int m_bits[];
	int m_size;
	
    CBitset()
    {
    	m_bits = null;
    	m_size= 0;
    }
    
	void Resize(int count)
	{
		m_size = count/32 + 1;
        
		/*
        if(m_bits != null)
		{
			delete m_bits;
			m_bits = 0;
		}*/
        
		m_bits = new int[m_size];
		ClearAll();
	}
    
	void Set(int i)
	{
		m_bits[i >> 5] |= (1 << (i & 31));
	}
    
	int On(int i)
	{
		return m_bits[i >> 5] & (1 << (i & 31 ));
	}
    
	void Clear(int i)
	{
		m_bits[i >> 5] &= ~(1 << (i & 31));
	}
    
	void ClearAll()
	{
		//memset(m_bits, 0, sizeof(unsigned int) * m_size);
		
		for(int i=0; i<m_size; i++)
			m_bits[i] = 0x00000000;
	}
};
