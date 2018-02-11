package com.websvc.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import com.websvc.MediaInfo;
import com.websvc.MediaInfoBuilder;
import com.websvc.dao.MediaInfoRepository;
import com.websvc.services.MediaInfoService;
import com.websvc.services.MediaInfoServiceImpl;
import com.websvc.services.StorageProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")//this line is to prevent the CommandLineRunner from being called
//@RunWith(SpringRunner.class)
@SpringBootTest
public class MediaInfoServiceIntegrationTest {
	
    private StorageProperties properties = new StorageProperties();
    @MockBean
    private MediaInfoService service;
    @MockBean
	private MediaInfoRepository mediaInfoRepository;
	
    @Before
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new MediaInfoServiceImpl(properties, mediaInfoRepository);
        service.init();
    }
    
	@Test
	public void testLoadingAllMediaInfosFromDiskIntoTheDatabaseSuccessfully() throws IOException {
		//GIVEN a list of files stored on disk
        Path file1 = Paths.get(properties.getLocation()+"/foo1.mp3");
        Path file2 = Paths.get(properties.getLocation()+"/foo2.mp3");
        Path file3 = Paths.get(properties.getLocation()+"/foo3.mp3");

        Files.write(file1, new byte[0], StandardOpenOption.CREATE);
        Files.write(file2, new byte[0], StandardOpenOption.CREATE);
        Files.write(file3, new byte[0], StandardOpenOption.CREATE);
		
		//WHEN loading all media infos from disk
		service.loadMediaFilesFromDisk();
		
		//THEN all media infos have been stored in h2
		verify(mediaInfoRepository, times(1)).save(argThat(new IsMediaInfoWithPathAndFileName(file1.toAbsolutePath().toString(), "foo1.mp3")));
		verify(mediaInfoRepository, times(1)).save(argThat(new IsMediaInfoWithPathAndFileName(file2.toAbsolutePath().toString(), "foo2.mp3")));
		verify(mediaInfoRepository, times(1)).save(argThat(new IsMediaInfoWithPathAndFileName(file3.toAbsolutePath().toString(), "foo3.mp3")));
	}
	
	@Test
	public void testLoadingAllMediaInfosFromDiskFiltersNonMp3Files() throws IOException {
		//GIVEN a list of files stored on disk
        Path file1 = Paths.get(properties.getLocation()+"/foo1.nonmp3");
        Path file2 = Paths.get(properties.getLocation()+"/foo2.mp3");
        Path file3 = Paths.get(properties.getLocation()+"/foo3.mp3");

        Files.write(file1, new byte[0], StandardOpenOption.CREATE);
        Files.write(file2, new byte[0], StandardOpenOption.CREATE);
        Files.write(file3, new byte[0], StandardOpenOption.CREATE);
		
		//WHEN loading all media infos from disk
		service.loadMediaFilesFromDisk();
		
		//THEN only mp3 files have been stored in h2
		verify(mediaInfoRepository, times(1)).save(argThat(new IsMediaInfoWithPathAndFileName(file2.toAbsolutePath().toString(), "foo2.mp3")));
		verify(mediaInfoRepository, times(1)).save(argThat(new IsMediaInfoWithPathAndFileName(file3.toAbsolutePath().toString(), "foo3.mp3")));
		//the non-mp3 file should not have been saved
		verify(mediaInfoRepository, times(0)).save(argThat(new IsMediaInfoWithPathAndFileName(file1.toAbsolutePath().toString(), "foo1.nonmp3")));
	}
	
	@Test
	public void testFindMediaInfoById() {
		//GIVEN a media infos in our database
		MediaInfo expected = new MediaInfoBuilder("fake-path").withFileName("1.mp3").withId(4L).build();
		given(this.mediaInfoRepository.findOne(4L)).willReturn(expected);
        
		//WHEN retrieving a single media info object by ID
        MediaInfo retrievedMediaInfo = service.findById(4L);
		//THEN the repository is queried for the correct mediaInfo
		then(mediaInfoRepository).should(times(1)).findOne(4L);
		assertEquals("Ids should be equal", expected.getId(), retrievedMediaInfo.getId());		
		assertEquals("Paths should be equal", expected.getPath(), retrievedMediaInfo.getPath());
	}
	
	@Test
	public void testStoreMediaInfoCopiesFileToDiskAndSavesInDatabase() {
		//GIVEN a mp3 file that we need to save
		String expectedFileName = "everlong.mp3";
		MultipartFile multiPartFile = new MockMultipartFile("everlong", expectedFileName, MediaType.TEXT_PLAIN_VALUE,
                "Hello World".getBytes());
		
		//WHEN storing the file
		MediaInfo savedMediaInfo = service.store(multiPartFile);
		
		//THEN a copy of the file should be stored on disk and information saved to the database
        assertThat(service.load("everlong.mp3")).exists();
        then(mediaInfoRepository).should(times(1)).save(any(MediaInfo.class));
        assertThat(savedMediaInfo.getPath().contains(properties.getLocation()+expectedFileName));
        assertThat(savedMediaInfo.getFileName().equals(expectedFileName));
	}
	
	@Test
	public void testListAllMediaInfosStored() {
		//GIVEN a list of media infos in our database
		List<MediaInfo> expectedList = new ArrayList<MediaInfo>();
		MediaInfo expected1 = new MediaInfoBuilder("fake-path-1").withFileName("1.mp3").withId(1L).build();
		MediaInfo expected2 = new MediaInfoBuilder("fake-path-2").withFileName("1.mp3").withId(2L).build();
		MediaInfo expected3 = new MediaInfoBuilder("fake-path-3").withFileName("1.mp3").withId(3L).build();
		expectedList.add(expected1);
		expectedList.add(expected2);
		expectedList.add(expected3);
		given(this.mediaInfoRepository.findAll()).willReturn(expectedList);
		
		//WHEN retrieving the list of media infos
		List<MediaInfo> retrievedList = service.findAll();
		
		//THEN the results of the retrieval should match what in the database
		then(mediaInfoRepository).should(times(1)).findAll();
		assertEquals("Number of mediaItems stored should match", expectedList.size(), retrievedList.size());		
		assertThat( retrievedList, contains(
			    hasProperty("path", is(expected1.getPath())), 
			    hasProperty("path", is(expected2.getPath())),
			    	hasProperty("path", is(expected3.getPath()))
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

	class IsMediaInfoWithPathAndFileName extends ArgumentMatcher<MediaInfo> {
		String expectedPath;
		String expectedName;
		public IsMediaInfoWithPathAndFileName(String expectedPath, String expectedName) {
			this.expectedPath = expectedPath;
			this.expectedName = expectedName;
		}
		public boolean matches(Object mediaInfo) {
			return ((MediaInfo) mediaInfo).getPath().equals(expectedPath) && ((MediaInfo) mediaInfo).getFileName().equals(expectedName);
		}
	}
}
