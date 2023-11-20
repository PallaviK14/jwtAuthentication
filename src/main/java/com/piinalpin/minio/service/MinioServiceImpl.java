package com.piinalpin.minio.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.piinalpin.minio.http.dto.FileDto;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

@Slf4j
@Service
public class MinioServiceImpl {

    @Autowired
    private MinioClient minioClient;
    
 

    @Value("${minio.bucket.name}")
    private String bucketName;

    public List<FileDto> getListObjects() {
        List<FileDto> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            for (Result<Item> item : result) {
                objects.add(FileDto.builder()
                        .filename(item.get().objectName())
                        .size(item.get().size())
                        .url(getPreSignedUrl(item.get().objectName()))
                        .build());
            }
            return objects;
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
        }

        return objects;
    }

    public InputStream getObject(String filename) {
        InputStream stream;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
            return null;
        }

        return stream;
    }

    public FileDto uploadFile(FileDto request) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(request.getFile().getOriginalFilename())
                    .stream(request.getFile().getInputStream(), request.getFile().getSize(), -1)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when upload file: ", e);
        }
        return FileDto.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .size(request.getFile().getSize())
                .url(getPreSignedUrl(request.getFile().getOriginalFilename()))
                .filename(request.getFile().getOriginalFilename())
                .build();
    }

    
    
    public String getObjectPaths(String fileName) throws Exception{
        List<String> objectPaths = new ArrayList<>();
        try {

        	// Generate a pre-signed URL with an expiry time (e.g., 1 hour)
            String objectURL = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET) // Specify the HTTP method (GET for downloading)
                    .bucket(bucketName)
                    .object(fileName)
                    .expiry(1, TimeUnit.HOURS) // Set the expiration time
                    .build()
            );

            return objectURL;
        } catch (MinioException | IllegalArgumentException e) {
            e.printStackTrace();
            // Handle exceptions here
            return null;
        }
    }

    
    
    
    public MultimediaInfo getInfo(String videoUrl) throws MalformedURLException, EncoderException {

    	
    	URL url = new URL(videoUrl);

    	try {

    	MultimediaObject multimediaObject = new MultimediaObject(url);

    	MultimediaInfo multimediaInfo = multimediaObject.getInfo();

    	return multimediaInfo;

    	}catch (InputFormatException e){

    	e.printStackTrace();

    	return null;

    	}
    }
  
    
    
    private String getPreSignedUrl(String filename) {
        return "http://localhost:8080/file/".concat(filename);
    }

}
