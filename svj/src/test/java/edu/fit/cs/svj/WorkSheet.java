package edu.fit.cs.svj;

import org.junit.Test;

public class WorkSheet {
	
	@Test
	public void countLoop() {
		final int width = 128;
		final int height = 128;
		final int ww = 24;
		final int wh = 24;
		final int bw = width - 10;
		final int bh = height - 10;
		
		double scale = 0.2 + 1;
		int count = 0;
		
		for (double i = 1.0; (i * ww) < bw && (i * wh) < bh; i *= scale) {
			count++;
		}
		
		System.out.println(count);
	}

}
