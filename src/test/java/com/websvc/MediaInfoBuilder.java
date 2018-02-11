package com.websvc;

import com.websvc.MediaInfo;

public class MediaInfoBuilder {

	private MediaInfo mediaInfo;
	
	public MediaInfoBuilder(String path) {
		mediaInfo = new MediaInfo(path);
	}
	
	public MediaInfoBuilder withId(long id) {
		this.mediaInfo.setId(id);
		return this;
	}
	
	public MediaInfoBuilder withFileName(String fileName) {
		this.mediaInfo.setFileName(fileName);
		return this;
	}
	
	public MediaInfoBuilder withArtist(String artist) {
		this.mediaInfo.setArtist(artist);
		return this;
	}
	
	public MediaInfoBuilder withTitle(String title) {
		this.mediaInfo.setTitle(title);
		return this;
	}
	
	public MediaInfoBuilder withGenre(String genre) {
		this.mediaInfo.setGenre(genre);
		return this;
	}
	
	public MediaInfoBuilder withTrackNumber(String trackNumber) {
		this.mediaInfo.setTrackNumber(trackNumber);
		return this;
	}
	
	public MediaInfoBuilder withReleaseDate(String releaseDate) {
		this.mediaInfo.setReleaseDate(releaseDate);
		return this;
	}
	
	public MediaInfo build() {
		return mediaInfo;
	}
}
