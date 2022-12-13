package org.jdoo.base.models;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdoo.*;

@Model.Meta(name = "ir.attachment", label = "附件", order = "id desc", description = "附件")
public class IrAttachment extends Model {

    static Field name = Field.Char().label("名称").index(true).required(true);
    static Field description = Field.Text().label("说明");
    static Field res_name = Field.Char().label("资源名称")
            .compute(Callable.method("ComputeResName"));
    static Field res_model = Field.Char().label("资源模型").readonly(true);
    static Field res_field = Field.Char().label("资源字段").readonly(true);
    static Field res_id = Field.Many2oneReference("res_model").label("资源ID").readonly(true);
    static Field company_id = Field.Many2one("res.company").label("公司").defaultValue(Default.method("CompanyDefault"));
    static Field type = Field.Selection(Selection.value(new HashMap<String, String>(16) {
        {
            put("url", "URL");
            put("binary", "File");
        }
    })).label("类型").required(true).defaultValue("binary").help("你可以从你的电脑上传一个文件或复制/粘贴一个链接。 ");
    static Field url = Field.Char().label("Url").readonly(true).length(1024);
    static Field is_public = Field.Boolean().label("公共文档");
    static Field access_token = Field.Char().label("访问TOKEN");
    static Field raw = Field.Binary().label("文件内容(raw)").compute("ComputeRaw")
            .depends("store_fname", "db_datas");
    static Field datas = Field.Binary().label("文件内容(base64)").compute("ComputeDatas")
            .depends("store_fname", "db_datas", "file_size");
    static Field db_datas = Field.Binary().label("数据库数据").attachment(false);
    static Field store_fname = Field.Char().label("文件名").index(true);
    static Field file_size = Field.Integer().label("文件大小").readonly(true);
    static Field checksum = Field.Char().label("摘要").index(true).length(40).readonly(true);
    static Field mimetype = Field.Char().label("文件类型").readonly(true);
    static Field index_content = Field.Char().label("索引内容").readonly(true).prefetch(false);

    public String ComputeResName(Records rec) {
        String res_name = "";
        for (Records r : rec) {
            String resModel = (String) r.get("res_model");
            String resId = (String) r.get("res_id");
            if (resModel != null && !resModel.isEmpty() && resId != null && !resId.isEmpty()) {
                Records record = r.getEnv().get(resModel).browse(resId);
                res_name = (String) record.get("display_name");
                r.set("res_name", res_name);
            } else {
                r.set("res_name", "");
            }
        }
        return res_name;
    }

    public void _post_add_create(Records rec) {

    }

    /**
     * 默认公司
     *
     * @param rec
     * @return
     */
    public String CompanyDefault(Records rec) {
        return rec.getEnv().getCompany().getId();
    }

    public Object ComputeDatas(Records rec) {
        Object bin_size = rec.getEnv().getContext().get("bin_size");

        if (bin_size != null) {
            for (Records r : rec) {
                String okdata = (String) r.get("name");
                r.set("datas", okdata);
                return okdata;
            }
        } else {
            for (Records r : rec) {
                byte[] data = (byte[]) r.get("raw");
                Object okData = new String(data);
                r.set("datas", okData);
                return okData;
            }
        }
        return null;
    }

    public Object ComputeRaw(Records rec) {
        for (Records r : rec) {
            String storeFname = (String) r.get("store_fname");
            if (storeFname != null && !storeFname.isEmpty()) {
                Object data = this.FileRead(storeFname);
                r.set("raw", data);
                return data;
            } else {
                Object data = r.get("db_datas");
                r.set("raw", data);
                return data;
            }
        }
        return null;
    }

    /**
     * 读取文件
     *
     * @param storeFname
     * @return
     */
    private byte[] FileRead(String storeFname) {
        String fullPath = this.FullPath(storeFname);
        try {
            File file = new File(fullPath);
            byte[] bytes = Files.readAllBytes(file.toPath());
            return bytes;
        } catch (IOException ex) {
        } finally {

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Records createBatch(Records rec, List<Map<String, Object>> valuesList) {
        // List<Map<String, Object>> newValuesList = new ArrayList<Map<String,
        // Object>>();
        // //移除不必要的字段
        // for (Map<String, Object> r : valuesList) {
        // Map<String, Object> newR = r.entrySet().stream()
        // .filter(o -> !o.getKey().equals("file_size") &&
        // !o.getKey().equals("checksum")
        // && !o.getKey().equals("store_fname"))
        // .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // if (newR.size() > 0) {
        // newValuesList.add(newR);
        // }
        // }
        // 多个附件
        for (Map<String, Object> values : valuesList) {
            values = this.CheckContents(values);
            // 二进制数据
            Object raw = values.get("raw");
            // base64数据
            Object datas = values.get("datas");

            if (values.containsKey("raw")) {
                values.remove("raw");
            }
            if (values.containsKey("datas")) {
                values.remove("datas");
            }
            Object withData = raw != null ? raw : datas;
            if (withData != null) {
                Map<String, Object> withDataMap = null;
                if (withData instanceof Map) {
                    withDataMap = (Map<String, Object>) withData;
                } else {
                    withDataMap = new HashMap<>(8);
                    withDataMap.put("data", withData);
                }
                Map<String, Object> valuesRelated = this.GetDatasRelatedValues(withDataMap,
                        (String) values.get("mimetype"));
                for (Entry<String, Object> entry : valuesRelated.entrySet()) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }

        }
        return (Records) rec.callSuper(IrAttachment.class, "createBatch", valuesList);
    }

    private Map<String, Object> CheckContents(Map<String, Object> values) {
        if (!values.containsKey("mimetype")) {
            values.put("mimetype", ComputeMimetype(values));
        }
        return values;
    }

    /**
     * 计算数据类型
     *
     * @param values
     * @return
     */
    private String ComputeMimetype(Map<String, Object> values) {
        return "application/octet-stream";
    }

    /**
     * 处理元数据
     *
     * @param datas
     * @param mimetype
     * @return
     */
    private Map<String, Object> GetDatasRelatedValues(Map<String, Object> datas, String mimetype) {

        Object withData = datas.get("data");
        byte[] binData = null;
        if (withData instanceof String) {
            binData = ((String) withData).getBytes(StandardCharsets.UTF_8);
        } else if (withData instanceof byte[]) {
            binData = (byte[]) withData;
        }
        String checkSum = this.ComputeChecksum(binData);
        String indexContent = this.IndexContent(binData, mimetype, checkSum);
        Map<String, Object> values = new HashMap<>();
        if (binData != null) {
            values.put("file_size", binData.length);
        }
        values.put("checksum", checkSum);
        values.put("index_content", indexContent);
        values.put("store_fname", false);
        values.put("db_datas", binData);
        if (datas.containsKey("file_name")) {
            values.put("name", datas.get("file_name"));
        }

        if (binData != null && this.Storage() != "db") {
            values.put("store_fname", this.FileWrite(binData, checkSum));
            values.put("db_datas", null);
        }
        return values;
    }

    /**
     * @param binData
     * @return
     */
    private String ComputeChecksum(byte[] binData) {
        if (binData == null) {
            binData = new byte[] {};
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5 = md.digest(binData);
            // 将处理后的字节转成 16 进制，得到最终 32 个字符
            StringBuilder sb = new StringBuilder();
            for (byte b : md5) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {

        }
        return null;

    }

    private String IndexContent(byte[] binData, String fileType, String checkSum) {
        String indexContent = null;
        if (!"".equals(fileType)) {
            indexContent = fileType.split("/")[0];
        }
        return indexContent;
    }

    /**
     * 存储方法
     *
     * @return
     */
    private String Storage() {
        return "file";
    }

    /**
     * 写文件
     *
     * @param data
     * @param checksum
     * @return
     */
    private String FileWrite(byte[] data, String checksum) {
        String fullPath = GetPath(data, checksum);
        File file = new File(fullPath);
        // 存在文件不需要重新保存（文件名是根据内容hash）
        if (file.exists()) {
            return file.getName();
        }
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream output = null;
        try {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
            bis = new BufferedInputStream(byteInputStream);
            File path = file.getParentFile();
            if (!path.exists()) {
                path.mkdirs();
            }
            fos = new FileOutputStream(file);
            // 实例化OutputString 对象
            output = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length != -1) {
                output.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            output.flush();
        } catch (Exception e) {

        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e0) {
            }
        }
        return file.getName();
    }

    /**
     * 获取文件路径
     *
     * @param data
     * @param checksum
     * @return
     */
    private String GetPath(byte[] data, String checksum) {
        String fullPath = this.FullPath(checksum);
        File file = new File(fullPath);
        if (file.isFile()) {
            return fullPath;
        }
        return fullPath;
    }

    /**
     * 获取全路径
     *
     * @param checksum
     * @return
     */
    private String FullPath(String checksum) {
        String subPath = checksum.substring(0, 3);
        Path path = Paths.get("D:/Temp", subPath);
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        Path pathFull = Paths.get("D:/Temp", subPath, checksum);
        File fileFull = pathFull.toFile();
        return fileFull.getPath();
    }

    /**
     * 设置更新数据
     *
     * @param records
     * @param valuesMap
     */
    public void SetAttachmentData(Records records, HashMap<String, Object> valuesMap) {
        for (Records item : records) {

            Map<String, Object> values = GetDatasRelatedValues(valuesMap, (String) item.get("mimetype"));
            item.call("update", item, values);
        }

    }

}
