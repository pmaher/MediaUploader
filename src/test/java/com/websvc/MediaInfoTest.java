package com.websvc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.websvc.MediaInfo;

public class MediaInfoTest {

	@Test
	public void testMediaInfoConstructor() {
		//GIVEN a new MediaInfo Class
		MediaInfo mediaInfo;
		//WHEN instantiating a new MediaInfo Object
		mediaInfo = new MediaInfo("path-to-file/name", "name");
		//THEN the attributes should be set accordingly
		assertEquals("path-to-file/name", mediaInfo.getPath());
		assertEquals("name", mediaInfo.getFileName());
	}

}
