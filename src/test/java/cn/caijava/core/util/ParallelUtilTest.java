package cn.caijava.core.util;

import cn.caijava.core.util.annotation.BeanFieldRef;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class ParallelUtilTest extends TestCase {

    @BeanFieldRef(refResultClass = User.class, refResultFieldName = "username")
    public String getName(String dataPeriod, String orgCode) {
        return StringUtils.joinWith("-", dataPeriod, orgCode, "name");
    }

    @BeanFieldRef(refResultClass = User.class, refResultFieldName = "password")
    public String getPassword(String dataPeriod, String orgCode) {
        return StringUtils.joinWith("-", dataPeriod, orgCode, "password");
    }

    @Test
    public void test() {
        String dataPeriod = "202207";
        String orgCode = "911400001109313590";
        ParallelUtilTest service = new ParallelUtilTest();
        User user = ParallelUtil.parallelRunService(
                service,
                new User(),
                new Object[]{dataPeriod, orgCode});
        Assert.assertNotNull(user);
        Assert.assertTrue(StringUtils.joinWith("-", dataPeriod, orgCode, "name")
                .equals(user.getUsername()));
        Assert.assertTrue(StringUtils.joinWith("-", dataPeriod, orgCode, "password")
                .equals(user.getPassword()));
    }
}