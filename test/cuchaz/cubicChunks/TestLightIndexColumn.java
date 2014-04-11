/*******************************************************************************
 * Copyright (c) 2014 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.cubicChunks;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLightIndexColumn
{
	@Test
	public void readZero( )
	{
		LightIndexColumn index = new LightIndexColumn();
		for( int i=0; i<16; i++ )
		{
			assertEquals( 0, index.getOpacity( i ) );
		}
	}
	
	@Test
	public void writeBottomDiffAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 0 )
		} );
		
		index.setOpacity( 0, 1 );
		
		assertEquals( 1, index.getOpacity( 0 ) );
		assertEquals( 0, index.getOpacity( 1 ) );
	}
	
	@Test
	public void writeBottomSameAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 0 ),
			LightIndexColumn.pack( 1, 1 )
		} );
		
		index.setOpacity( 0, 1 );
		
		assertEquals( 1, index.getOpacity( 0 ) );
		assertEquals( 1, index.getOpacity( 1 ) );
	}
	
	@Test
	public void writeMiddleSameBottomSameAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 1 ),
			LightIndexColumn.pack( 5, 0 ),
			LightIndexColumn.pack( 6, 1 )
		} );
		
		index.setOpacity( 5, 1 );
		
		assertEquals( 1, index.getOpacity( 4 ) );
		assertEquals( 1, index.getOpacity( 5 ) );
		assertEquals( 1, index.getOpacity( 6 ) );
	}
	
	@Test
	public void writeMiddleDiffBottomSameAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 0 ),
			LightIndexColumn.pack( 6, 1 )
		} );
		
		index.setOpacity( 5, 1 );
		
		assertEquals( 0, index.getOpacity( 4 ) );
		assertEquals( 1, index.getOpacity( 5 ) );
		assertEquals( 1, index.getOpacity( 6 ) );
	}
	
	@Test
	public void writeMiddleDiffBottomDiffAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 0 ),
		} );
		
		index.setOpacity( 5, 1 );
		
		assertEquals( 0, index.getOpacity( 4 ) );
		assertEquals( 1, index.getOpacity( 5 ) );
		assertEquals( 0, index.getOpacity( 6 ) );
	}
	
	@Test
	public void writeMiddleSameBottomDiffAbove( )
	{
		LightIndexColumn index = new LightIndexColumn( new int[] {
			LightIndexColumn.pack( 0, 1 ),
			LightIndexColumn.pack( 6, 0 )
		} );
		
		index.setOpacity( 5, 1 );
		
		assertEquals( 1, index.getOpacity( 4 ) );
		assertEquals( 1, index.getOpacity( 5 ) );
		assertEquals( 0, index.getOpacity( 6 ) );
	}
}