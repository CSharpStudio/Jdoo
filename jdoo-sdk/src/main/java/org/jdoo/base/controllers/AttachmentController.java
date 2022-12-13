package org.jdoo.base.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.jdoo.Records;
import org.jdoo.https.Controller;
import org.jdoo.https.jsonrpc.JsonRpcResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@org.springframework.stereotype.Controller
@RestController
public class AttachmentController extends Controller {
 

    @RequestMapping("/{tenant}/attachment/upload")
    public Object uploadFile(@RequestParam("file") MultipartFile multipartFile,
            @RequestParam("thread_id") int threadId,
            @RequestParam("thread_model") String threadModel) throws IOException {
        Map<String, Object> val = new HashMap<>();
        val.put("name", multipartFile.getOriginalFilename());
        val.put("raw", multipartFile.getBytes());
        val.put("res_id", threadId);
        val.put("res_model", threadModel);

        Records attachment = getEnv().get("ir.attachment").create(val);
        attachment.call("_post_add_create");
        Map<String, Object> attachmentData = new HashMap<>();
        attachmentData.put("filename", multipartFile.getOriginalFilename());
        attachmentData.put("id", attachment.getId());
        attachmentData.put("mimetype", attachment.get("mimetype"));
        attachmentData.put("name", attachment.get("name"));
        attachmentData.put("size", attachment.get("file_size"));

        JsonRpcResponse data= new JsonRpcResponse();
        data.setResult(attachmentData);
        return data;
    }


    // @SuppressWarnings("unchecked")
    // @RequestMapping(value = "/{tenant}/attachment/upload", method = RequestMethod.POST)
    // @RequestHandler(auth = AuthType.NONE, type = HandlerType.HTTP)
    // public Object uploadFile(JsonRpcRequest request){
       
    //     return "attachmentData";
    // }

    // public Object uploadFile(@RequestParam("file") MultipartFile multipartFile,
    //         @RequestParam("thread_id") int threadId,
    //         @RequestParam("thread_model") String threadModel) throws IOException {
    //     Map<String, Object> val = new HashMap<>();
    //     val.put("name", multipartFile.getOriginalFilename());
    //     val.put("raw", multipartFile.getBytes());
    //     val.put("res_id", threadId);
    //     val.put("res_model", threadModel);

    //     Records attachment = getEnv().get("ir.attachment").create(val);
    //     attachment.call("_post_add_create");
    //     Map<String, Object> attachmentData = new HashMap<>();
    //     attachmentData.put("filename", multipartFile.getOriginalFilename());
    //     attachmentData.put("id", attachment.getId());
    //     attachmentData.put("mimetype", attachment.get("mimetype"));
    //     attachmentData.put("name", attachment.get("name"));
    //     attachmentData.put("size", attachment.get("file_size"));
    //     return new JsonRpcResponse(2,attachment);
    // }
}
