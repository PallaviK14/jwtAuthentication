package com.piinalpin.minio.http.controller;

import com.piinalpin.minio.http.dto.FileDto;
import com.piinalpin.minio.service.MinioServiceImpl;

import io.minio.ListObjectsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.Time;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.info.MultimediaInfo;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@Slf4j
@RestController
@RequestMapping(value = "/file")
public class FileController {

    @Autowired
    private MinioServiceImpl minioService;

    
    
    //get a list of files 
    @GetMapping
    public ResponseEntity<Object> getFiles() {
        return ResponseEntity.ok(minioService.getListObjects());
    }

    //get a particular file
    @GetMapping(value = "/**")
    public ResponseEntity<Object> getFile(HttpServletRequest request) throws IOException {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        String filename = new AntPathMatcher().extractPathWithinPattern(pattern, request.getServletPath());
        return ResponseEntity.ok()
        		.contentType(MediaType.MULTIPART_FORM_DATA)
                .body(IOUtils.toByteArray(minioService.getObject(filename)));
    }

    //upload a file
    @PostMapping(value = "/upload")
    public ResponseEntity<Object> upload(@ModelAttribute FileDto request) {
        return ResponseEntity.ok().body(minioService.uploadFile(request));
    }
    
  //upload a file
    @GetMapping("/metadata") 
    public ResponseEntity<String> getMetadata(String fileName)throws Exception{
    	
    	MultimediaInfo multimediaInfo = minioService.getInfo(minioService.getObjectPaths(fileName));
    	long minutes = (multimediaInfo.getDuration() / 1000) / 60;
    	long seconds = (multimediaInfo.getDuration() / 1000) % 60;
    	Map<String, String> metadata= multimediaInfo.getMetadata();
    	String list= minioService.getObjectPaths(fileName);
    	String duration= "Duration="+ minutes +" minutes"+ "\t"+seconds+" seconds";    	
    	return new ResponseEntity<>(duration+" "+metadata+" "+list,HttpStatus.OK);
    }
    
    
    

   
//    @GetMapping("/getbytes")
//    public byte[] readByteRangeNew(@PathVariable String filename,@PathVariable long start,@PathVariable long end) throws Exception {
//        Path path = Paths.get(minioService.getObjectPaths(filename), filename);
//        byte[] data = Files.readAllBytes(path);
//        System.out.println(data);
//        byte[] result = new byte[(int) (end - start) + 1];
//        System.arraycopy(data, (int) start, result, 0, (int) (end - start) + 1);
//        return result;
//    }
    
   
    
  
    
    @RequestMapping(value = "/index")
    public String index() {
       return "index";
    }
    
    
    

}
