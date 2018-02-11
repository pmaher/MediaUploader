package com.websvc.services;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;

import com.websvc.MediaInfo;

public interface MediaInfoService {

    void init();
    
    void loadMediaFilesFromDisk();

    MediaInfo store(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    void deleteAll();

	List<MediaInfo> findAll();

	MediaInfo findById(long l);

}
