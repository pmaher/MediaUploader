package com.websvc.services;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.websvc.MediaInfo;
import com.websvc.dao.MediaInfoRepository;
import com.websvc.services.MediaInfoServiceImpl;
import com.websvc.services.StorageProperties;

public class MediaServiceImplTest {
	
	MediaInfoServiceImpl service;
	
	StorageProperties properties;
	
	@Mock
	MediaInfoRepository repository;
	
	Path testResourceDirectory = Paths.get("src","test","resources");
	
	@Before
    public void init() {
		properties = new StorageProperties();
        service = new MediaInfoServiceImpl(properties, repository);
    }
	
	@Test
	public void testGetMediaInfoFromPath() {
		//GIVEN a path to a mp3 file we want to extract meta information from
		Path audioPathWithMetaInfo = Paths.get("src","test","resources", "hotel_california.mp3");
		//WHEN calling the mediaInfoService to extract said meta data
		MediaInfo mediaInfo = service.getMediaInfoFromPath(audioPathWithMetaInfo);
		//THEN the meta information in the MediaInfo object should match
		assertEquals("Title meta data should be populated", "Hotel California", mediaInfo.getTitle());
		assertEquals("Artist meta data should be populated", "Eagles", mediaInfo.getArtist());
		assertEquals("Genre meta data should be populated", "Rock", mediaInfo.getGenre());
		assertEquals("Year meta data should be populated", "1977", mediaInfo.getReleaseDate());
		assertEquals("Track Number meta data should be populated", "3", mediaInfo.getTrackNumber());
	}

}
