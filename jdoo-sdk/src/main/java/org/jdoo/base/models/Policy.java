package org.jdoo.base.models;

import java.util.List;
import java.util.Map;

import org.jdoo.*;

@Model.Meta(name = "rbac.policy", label = "密码策略")
public class Policy extends Model {
        static Field neet_letter = Field.Boolean().label("必须有字母");
        static Field neet_symbol = Field.Boolean().label("必须有特殊字符");
        static Field neet_number = Field.Boolean().label("必须有数字");
        static Field case_sensitivity = Field.Boolean().label("必须有大小写");
        static Field length = Field.Integer().label("最小密码长度");
        static Field failed_pwd_times = Field.Integer().label("允许错误次数");
        static Field lock_time = Field.Float().label("锁定时长(秒)");
        static Field pwd_validity = Field.Integer().label("密码有效期(天)");

        @ServiceMethod(label = "加载密码策略")
        public Map<String, Object> loadData(Records rec, List<String> fields) {
                return rec.getEnv().getRef("base.rbac_password_policy").read(fields).get(0);
        }
}
