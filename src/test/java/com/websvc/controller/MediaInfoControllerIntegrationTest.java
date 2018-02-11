package com.websvc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.websvc.MediaInfo;
import com.websvc.MediaInfoBuilder;
import com.websvc.services.MediaInfoService;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MediaInfoControllerIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;
	
    @Autowired
    private MockMvc mockMvc;

	@MockBean
	private MediaInfoService mediaInfoService;

	@LocalServerPort
	private int port;
	
	private MediaInfo mediaInfo1;
	private MediaInfo mediaInfo2;
	
	@Before
	public void init() {
		mediaInfo1 = new MediaInfoBuilder("/path/to/file/1")
						.withFileName("1.mp3")
						.withId(1)
						.withArtist("artist 1")
						.withTitle("1")
						.withGenre("rock")
						.withReleaseDate("2018")
						.withTrackNumber("3")
						.build();
		mediaInfo2 = new MediaInfoBuilder("/path/to/file/2")
						.withFileName("2.mp3")
						.withId(4)
						.withArtist("artist 2")
						.withTitle("1")
						.withGenre("gospel")
						.withReleaseDate("1938")
						.withTrackNumber("7")
						.build();
	}
	
	@Test
	public void testListUploadedFiles() throws Exception {
		//GIVEN a list of media info objects stored in the database
		List<MediaInfo> mediaInfoList = new ArrayList<MediaInfo>();
		mediaInfoList.add(mediaInfo1);
		mediaInfoList.add(mediaInfo2);
		given(this.mediaInfoService.findAll()).willReturn(mediaInfoList);
		
		//WHEN making an http request to the homepage
		MvcResult result = this.mockMvc.perform(get("/"))
			//THEN the homepage model is injected with the media info objects and displayed correctly
			.andExpect(status().isOk())
			.andExpect(model().attribute("mediaInfos", hasSize(2)))
			.andExpect(model().attribute("mediaInfos", hasItem(Matchers.hasProperty("path", equalTo("/path/to/file/1")))))
			.andExpect(model().attribute("mediaInfos", hasItem(Matchers.hasProperty("path", equalTo("/path/to/file/2")))))
			.andExpect(view().name("uploadForm"))
			.andReturn();
		
		assertThat(result.getResponse().getContentAsString()).contains(mediaInfo1.getFileName());
		assertThat(result.getResponse().getContentAsString()).contains(mediaInfo2.getFileName());
	}

	@Test
	public void testListingAllFiles() throws Exception {
		//GIVEN we have a list of media info objects to return
		List<MediaInfo> mediaInfoList = new ArrayList<MediaInfo>();
		mediaInfoList.add(mediaInfo1);
		mediaInfoList.add(mediaInfo2);
		given(this.mediaInfoService.findAll()).willReturn(mediaInfoList);

		//WHEN requesting all the media info objects from the webservice
		MvcResult result = this.mockMvc.perform(get("/api/mediainfos"))
		//THEN we should get a JSON representation of all the data
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].id", is(mediaInfo1.getId().intValue())))
			.andExpect(jsonPath("$[0].fileName", is(mediaInfo1.getFileName())))
			.andExpect(jsonPath("$[0].title", is(mediaInfo1.getTitle())))
			.andExpect(jsonPath("$[0].path", is(mediaInfo1.getPath())))
			.andExpect(jsonPath("$[0].artist", is(mediaInfo1.getArtist())))
			.andExpect(jsonPath("$[0].genre", is(mediaInfo1.getGenre())))
			.andExpect(jsonPath("$[0].releaseDate", is(mediaInfo1.getReleaseDate())))
			.andExpect(jsonPath("$[0].trackNumber", is(mediaInfo1.getTrackNumber())))		
			.andExpect(jsonPath("$[1].id", is(mediaInfo2.getId().intValue())))
			.andExpect(jsonPath("$[1].fileName", is(mediaInfo2.getFileName())))
			.andExpect(jsonPath("$[0].title", is(mediaInfo2.getTitle())))
			.andExpect(jsonPath("$[1].path", is(mediaInfo2.getPath())))
			.andExpect(jsonPath("$[1].artist", is(mediaInfo2.getArtist())))
			.andExpect(jsonPath("$[1].genre", is(mediaInfo2.getGenre())))
			.andExpect(jsonPath("$[1].releaseDate", is(mediaInfo2.getReleaseDate())))
			.andExpect(jsonPath("$[1].trackNumber", is(mediaInfo2.getTrackNumber())))	
			.andReturn();
		
		assertThat(result.getResponse().getContentAsString()).contains("/path/to/file/1");
	}
	
	@Test
	public void testUploadingFileViaWebInterface() throws Exception {
		//GIVEN a mp3 file to upload to the service
		ClassPathResource resource = new ClassPathResource("testupload.mp3", getClass());
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("file", resource);
		
		//WHEN uploading the file to the server via the web interface
		ResponseEntity<String> response = this.restTemplate.postForEntity("/", map, String.class);

		//THEN the http response is successful and the file is stored
		assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().toString())
				.startsWith("http://localhost:" + this.port + "/");
		then(mediaInfoService).should().store(any(MultipartFile.class));
	}
	
	@Test
	public void testUploadingEmptyFileResultsInErrorMessage() throws Exception {
		//GIVEN an empty post request
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/");

		//WHEN performing said request
		this.mockMvc.perform(builder)
			//THEN we should get an error message back from the server
	        .andDo(MockMvcResultHandlers.print())
	        .andExpect(MockMvcResultMatchers.flash().attribute("message", "Please select a file to upload by clicking the Browse button."));
	}
	
	@Test
	public void testUploadingFileViaRestInterface() throws Exception {
		//GIVEN a mp3 file to upload to the service		
		MockMultipartFile firstFile = new MockMultipartFile("file", "filename.mp3", "text/plain", "some xml".getBytes());
		given(this.mediaInfoService.store(any(MultipartFile.class))).willReturn(mediaInfo1);
		
		//WHEN uploading the file to the server via the rest api
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/mediainfos", "file")
                .file(firstFile)
                .header("Content-type", "multipart/form-data"))
				//THEN we should get a JSON representation of all the data
	            .andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(mediaInfo1.getId().intValue())))
				.andExpect(jsonPath("$.path", is(mediaInfo1.getPath())))
				.andExpect(jsonPath("$.fileName", is(mediaInfo1.getFileName())))
				.andExpect(jsonPath("$.title", is(mediaInfo1.getTitle())))
				.andExpect(jsonPath("$.artist", is(mediaInfo1.getArtist())))
				.andExpect(jsonPath("$.genre", is(mediaInfo1.getGenre())))
				.andExpect(jsonPath("$.trackNumber", is(mediaInfo1.getTrackNumber())))
				.andExpect(jsonPath("$.releaseDate", is(mediaInfo1.getReleaseDate())));
	}

	@Test
	public void testFindingMediaInfoFileById() throws Exception {
		//GIVEN we have a mediaInfo object to query by
		given(this.mediaInfoService.findById(any(Long.class))).willReturn(new MediaInfoBuilder("/path/to/file/2").withFileName("2.mp3").withId(4L).build());

		//WHEN querying for a single mediaInfo object by ID
		this.mockMvc.perform(get("/api/mediainfos/{mediaInfoId}", 4))
			//THEN we should get a JSON representation of the correct mediaInfo file
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(4)))
			.andExpect(jsonPath("$.path", is("/path/to/file/2")));
	}

}
