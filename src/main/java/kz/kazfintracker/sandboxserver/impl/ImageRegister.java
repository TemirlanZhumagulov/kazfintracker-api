package kz.kazfintracker.sandboxserver.impl;

import kz.kazfintracker.sandboxserver.model.mongo.ImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageRegister {

  @Autowired
  private MongoTemplate mongoTemplate;

  public ResponseEntity<String> uploadImage(MultipartFile file) {
    try {
      saveImage(file);
      return ResponseEntity.status(HttpStatus.OK).body("Image uploaded successfully");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
    }
  }

  private void saveImage(MultipartFile file) throws IOException {
    ImageDto image = new ImageDto();
    image.setName(file.getOriginalFilename());
    image.setData(file.getBytes());
    mongoTemplate.save(image);
  }

}
