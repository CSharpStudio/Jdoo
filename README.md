### Java版本odoo


###  核心设计思想
    一切皆模型，所有模型皆可扩展

### 开源协议
    LGPL v3.0

### 模型示例代码

```java
package org.jdoo.models;

import org.jdoo.*;

@Model.Meta(name = "test.model", description = "模型元数据")
public class TestModel extends Model {
    static Field name = Field.Char().label("名称").help("模型名称")
        .index(true).required(true);//.translate();
    static Field description = Field.Char().label("描述").help("模型说明");
    static Field inherit = Field.Char().label("继承").help("模型的继承，多个使用逗号','分隔");
    static Field type = Field.Selection().label("类型").help("模型的类型：普通、抽象、瞬态");
    static Field type_name = Field.Char().label("类名")
        .compute(Callable.script("r->r.get('name')+'('+r.get('type')+')'"))
        .store(false).depends("name", "type");

    /** 获取名称 */
    public String getName() {
        return (String) get(name);
    }

    /** 设置名称 */
    public void setName(String value) {
        set(name, value);
    }

    /** 获取描述 */
    public String getDescription() {
        return (String) get(description);
    }

    /** 设置描述 */
    public void setDescription(String value) {
        set(description, value);
    }

    /** 获取继承 */
    public String getInherit() {
        return (String) get(inherit);
    }

    /** 设置继承 */
    public void setInherit(String value) {
        set(inherit, value);
    }

    /** 获取类型 */
    public String getType() {
        return (String) get(type);
    }

    /** 设置类型 */
    public void setType(String value) {
        set(type, value);
    }	

    /** 获取类型名称 */
    public String getTypeName() {
        return (String) get(type_name);
    }

    /** Model method demo */
    @Model.ServiceMethod
    public void test(Records rec) {
        for (TestModel testModel : rec.of(TestModel.class)) {
            testModel.setType("integer");
            System.out.println(testModel.getName());
        }
        //do something else
    }
}
```

应用安装示例：
https://github.com/CSharpStudio/Jdoo/blob/main/apps.mp4?raw=true

低代码示例：
https://github.com/CSharpStudio/Jdoo/blob/main/lowcode.mp4?raw=true

API文档：
![api文档](https://github.com/CSharpStudio/Jdoo/blob/main/resources/api-doc.screenshots.png?raw=true)

登录界面：
![登录界面](https://github.com/CSharpStudio/Jdoo/blob/main/resources/login.screenshots.png?raw=true)

表格界面：
![表格界面](https://github.com/CSharpStudio/Jdoo/blob/main/resources/grid.screenshots.png?raw=true)

编辑界面：
![编辑界面](https://github.com/CSharpStudio/Jdoo/blob/main/resources/form.screenshots.png?raw=true)
