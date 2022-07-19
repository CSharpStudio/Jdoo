package org.jdoo.base.models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import org.jdoo.*;
import org.jdoo.util.KvMap;
import org.apache.commons.lang3.time.DateUtils;

@Model.Meta(name = "rbac.token", description = "令牌", logAccess = BoolState.False)
public class Token extends Model {
    static Field user_id = Field.Many2one("rbac.user");
    static Field token = Field.Char();
    static Field date = Field.DateTime();
    static Field expiration = Field.DateTime();

    public String newToken(Records rec, String uid) {
        String uuid = UUID.randomUUID().toString();
        // 使用uid加密生成
        return uuid;
    }

    public String getUserId(Records rec, String token) {
        Records r = rec.find(Criteria.equal("token", token));
        if (r.any()) {
            Date now = new Date();
            Timestamp t = (Timestamp) r.get("expiration");
            if (t.before(now)) {
                return null;
            }
            String uid = ((Records) r.get("user_id")).getId();
            if (t.before(DateUtils.addHours(now, 1))) {
                String uuid = newToken(rec, uid);
                r.set("token", uuid);
                r.set("date", now);
                r.set("expiration", DateUtils.addHours(now, 8));
                rec.getEnv().getContext().put("token", uuid);
            }
            return uid;
        }
        return null;
    }

    public String updateToken(Records rec, String uid, int minutes) {
        Records r = rec.find(Criteria.equal("user_id", uid));
        String token = newToken(rec, uid);
        Date now = new Date();
        // TODO token限制id使用
        if (!r.any()) {
            r.create(new KvMap()
                    .set("user_id", uid)
                    .set("token", token)
                    .set("date", now)
                    .set("expiration", DateUtils.addMinutes(now, minutes)));
        } else {
            r.set("token", token);
            r.set("date", now);
            r.set("expiration", DateUtils.addMinutes(now, minutes));
        }
        return token;
    }
}
