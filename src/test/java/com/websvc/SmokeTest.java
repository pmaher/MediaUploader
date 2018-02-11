package com.websvc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.websvc.controller.MediaInfoController;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class SmokeTest {
	
	@Autowired
	private MediaInfoController fileUploadController;

    @Test
    public void contexLoads() throws Exception {
        assertThat(fileUploadController).isNotNull();
    }

}
