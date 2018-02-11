package com.websvc.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.websvc.MediaInfo;
import com.websvc.services.MediaInfoService;

@Controller
public class MediaInfoController {

    private final MediaInfoService mediaInfoService;

    @Autowired
    public MediaInfoController(MediaInfoService service) {
        this.mediaInfoService = service;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("mediaInfos", mediaInfoService.findAll());
        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/mediainfos", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public List<MediaInfo> listAll() {
		return this.mediaInfoService.findAll();
	}
    
    @RequestMapping(method = RequestMethod.GET, value = "/api/mediainfos/{mediaInfoId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
	public MediaInfo findMediaInfoById(@PathVariable("mediaInfoId") long mediaInfoId) {
		return this.mediaInfoService.findById(mediaInfoId);
	}

    @PostMapping("/")
    public String handleFileUpload(@RequestParam(value = "file", required = false) MultipartFile file, RedirectAttributes redirectAttributes) {
    		
    		if(file != null  && !file.isEmpty()) {
    			MultipartFile multipartFile = (MultipartFile) file;
    			mediaInfoService.store(multipartFile);
    			redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + multipartFile.getOriginalFilename() + "!");
    			redirectAttributes.addFlashAttribute("messageClass", "alert alert-success");
    		} else {
    			redirectAttributes.addFlashAttribute("message", "Please select a file to upload by clicking the Browse button.");
    			redirectAttributes.addFlashAttribute("messageClass", "alert alert-danger");
    		}

        return "redirect:/";
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/api/mediainfos", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public MediaInfo handleRESTFileUpload(@RequestParam("file") MultipartFile file) {

        return mediaInfoService.store(file);
    }

}
