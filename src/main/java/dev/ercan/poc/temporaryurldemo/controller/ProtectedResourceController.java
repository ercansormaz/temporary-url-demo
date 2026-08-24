package dev.ercan.poc.temporaryurldemo.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protected")
public class ProtectedResourceController {

  @GetMapping("/{id}")
  public String protectedContentGet(@PathVariable String id) {
    return "GET Request Allowed for " + id;
  }

  @PostMapping("/{id}")
  public String protectedContentPost(@PathVariable String id) {
    return "POST Request Allowed for " + id;
  }

  @PutMapping("/{id}")
  public String protectedContentPut(@PathVariable String id) {
    return "PUT Request Allowed for " + id;
  }

  @PatchMapping("/{id}")
  public String protectedContentPatch(@PathVariable String id) {
    return "PATCH Request Allowed for " + id;
  }

  @DeleteMapping("/{id}")
  public String protectedContentDelete(@PathVariable String id) {
    return "DELETE Request Allowed for " + id;
  }

}
