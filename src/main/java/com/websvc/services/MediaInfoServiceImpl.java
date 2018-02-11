package com.websvc.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.websvc.MediaInfo;
import com.websvc.dao.MediaInfoRepository;

@Service
public class MediaInfoServiceImpl implements MediaInfoService {

    private final Path rootLocation;
    
    private MediaInfoRepository mediaInfoRepository;
    
    public static final String MEDIA_FILE_EXT = ".mp3";

    @Autowired
    public MediaInfoServiceImpl(StorageProperties properties, MediaInfoRepository mediaInfoRepository) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.mediaInfoRepository = mediaInfoRepository;
    }
    
    @Override
    //This is called on application load so we can populate our in memory database with existing file data
    public void loadMediaFilesFromDisk() {
        try {
            		Files.walk(this.rootLocation, 1)
            			//make sure to exclude the root and any non-mp3 files
                    .filter(path -> !path.equals(this.rootLocation) && path.toString().endsWith(MEDIA_FILE_EXT) )
                    .forEach(path -> {
                    		MediaInfo mediaInfoToSave = getMediaInfoFromPath(path);
                    		mediaInfoRepository.save(mediaInfoToSave);
                    		//mediaInfoRepository.save(new MediaInfo(path.toAbsolutePath().toString(), path.getFileName().toString()));
                    });
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }
    
    @Override
	public MediaInfo findById(long id) {
		return mediaInfoRepository.findOne(id);
	}

    @Override
    public MediaInfo store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        MediaInfo newMediaInfo;
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            Path newpath = this.rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), newpath,
                    StandardCopyOption.REPLACE_EXISTING);
            newMediaInfo = getMediaInfoFromPath(newpath);
            mediaInfoRepository.save(newMediaInfo);
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
        return newMediaInfo;
    }
    
    protected MediaInfo getMediaInfoFromPath(Path path) {
		InputStream input;
		MediaInfo mediaInfo = new MediaInfo(path.toAbsolutePath().toString(), path.getFileName().toString());
		try {
			input = new FileInputStream(new File(path.toAbsolutePath().toString()));
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			Parser parser = new Mp3Parser();
			ParseContext parseCtx = new ParseContext();
			parser.parse(input, handler, metadata, parseCtx);
			input.close();
			
			// Retrieve the necessary info from metadata
			// Names - title, xmpDM:artist etc. - mentioned below may differ based
			// on the standard used for processing and storing standardized and/or
			// proprietary information relating to the contents of a file.
			if(metadata.get("xmpDM:trackNumber") != null) {
				mediaInfo.setTrackNumber(metadata.get("xmpDM:trackNumber"));
			}
			if(metadata.get("xmpDM:releaseDate") != null) {
				mediaInfo.setReleaseDate(metadata.get("xmpDM:releaseDate"));
			}
			if(metadata.get("xmpDM:artist") != null) {
				mediaInfo.setArtist(metadata.get("xmpDM:artist"));
			}
			if(metadata.get("xmpDM:genre") != null) {
				mediaInfo.setGenre(metadata.get("xmpDM:genre"));
			}
			if(metadata.get("title") != null) {
				mediaInfo.setTitle(metadata.get("title"));
			}
		} catch (IOException | SAXException | TikaException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}

    		return mediaInfo;
    }
    
    @Override
    public List<MediaInfo> findAll() {
    		return (List<MediaInfo>) mediaInfoRepository.findAll();
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
