package com.websvc.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.websvc.MediaInfo;
import com.websvc.dao.MediaInfoRepository;
import com.websvc.services.MediaInfoService;
import com.websvc.services.MediaInfoServiceImpl;
import com.websvc.services.StorageProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MediaInfoRepositoryIntegrationTest {
	
    private StorageProperties properties = new StorageProperties();
    
	@Autowired
	private MediaInfoRepository mediaInfoRepository;
	
    @MockBean
	private MediaInfoService mediaInfoService;
	
    @Before
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        mediaInfoService = new MediaInfoServiceImpl(properties, mediaInfoRepository);
        mediaInfoService.init();
    }

	@Test
	public void testSavingAndRetrievingAMediaInfo() {
		//GIVEN a mediaInfo saved in our database		
		MediaInfo mediaInfo = mediaInfoRepository.save(new MediaInfo("my/first/path/name", "name.mp3"));
		//WHEN retrieving that mediaInfo by ID
		MediaInfo retrievedMediaInfo = mediaInfoRepository.findOne(mediaInfo.getId());
		
		//THEN the mediaInfo information should match
		assertNotNull(retrievedMediaInfo);
		assertEquals(mediaInfo.getPath(), retrievedMediaInfo.getPath());
		assertEquals(mediaInfo.getFileName(), retrievedMediaInfo.getFileName());
	}
	
	@Test
	public void testListAllMediaInfos() {
		//GIVEN multiple mediaInfos saved in our database
		MediaInfo mediaInfo = mediaInfoRepository.save(new MediaInfo("my/first/path/name", "name1.mp3"));
		MediaInfo mediaInfo2 = mediaInfoRepository.save(new MediaInfo("my/second/path/name", "name2.mp3"));
		MediaInfo mediaInfo3 = mediaInfoRepository.save(new MediaInfo("my/third/path/name", "name3.mp3"));
		
		//WHEN retrieving all mediaInfos available
		List<MediaInfo> mediaInfos = (List<MediaInfo>) mediaInfoRepository.findAll();
		
		//THEN all the mediainfos should be found
		assertEquals("All mediaInfo objects should be found.", 3, mediaInfos.size());
		assertThat( mediaInfos, contains(
			    hasProperty("path", is(mediaInfo.getPath())), 
			    hasProperty("path", is(mediaInfo2.getPath())),
			    	hasProperty("path", is(mediaInfo3.getPath()))
			));
		assertThat( mediaInfos, contains(
			    	hasProperty("fileName", is(mediaInfo.getFileName())), 
				hasProperty("fileName", is(mediaInfo2.getFileName())),
				hasProperty("fileName", is(mediaInfo3.getFileName()))
			));
	}
	
    @After
    public void tearDown() throws IOException {

        Path rootPath = Paths.get(properties.getLocation());
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

}
