package org.jdoo.base.models;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.jdoo.*;
import org.jdoo.util.KvMap;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 令牌，记录用户登录的令牌
 * 
 * @author lrz
 */
@Model.Meta(name = "rbac.token", label = "令牌", logAccess = BoolState.False)
@Model.Service(remove = "@all")
public class RbacToken extends Model {
    static Field user_id = Field.Many2one("rbac.user");
    static Field token = Field.Char();
    static Field date = Field.DateTime();
    static Field expiration = Field.DateTime();

    /**
     * 生成新token。uuid+yyyyMMddHHmmss+uid
     * 
     * @param rec
     * @param uid
     * @return
     */
    public String newToken(Records rec, String uid) {
        String data = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + uid;
        byte[] bytes = data.getBytes();
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.allocate(bytes.length + 16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        bb.put(bytes);
        return Base64.getEncoder().encodeToString(bb.array());
    }

    /**
     * 根据令牌查询用户。
     * 
     * @param rec
     * @param token 令牌
     * @return 令牌不存在或者已过期，返回null
     */
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
                r.set("expiration", DateUtils.addHours(now, 8));
            }
            return uid;
        }
        return null;
    }

    /**
     * 更新令牌
     * 
     * @param rec
     * @param uid
     * @param minutes
     * @return
     */
    public String updateToken(Records rec, String uid, int minutes) {
        Records r = rec.find(Criteria.equal("user_id", uid));
        String token = newToken(rec, uid);
        Date now = new Date();
        // TODO token限制ip使用
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

    /**
     * 删除令牌
     * 
     * @param rec
     * @param uid
     */
    public void removeToken(Records rec, String uid) {
        Records r = rec.find(Criteria.equal("user_id", uid));
        r.delete();
    }
}
