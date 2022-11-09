package cn.caijava.core.util;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundOffUtilTest extends TestCase {

    @Test
    public void test() {
        RoundTestBean bean = new RoundTestBean();
        bean.setA1("人才");
        bean.setA2("1");
        bean.setB(1.234567F);
        bean.setC(2.234567F);
        bean.setD(3.234567D);
        bean.setE(4.234567D);
        bean.setF(new BigDecimal("5.234567"));

        RoundTestBean bean2 = new RoundTestBean();
        bean2.setA1("77.7777");
        bean.setInner(bean2);

        RoundTestBean bean3 = new RoundTestBean();
        bean3.setA1("88.88888");
        RoundTestBean bean4 = new RoundTestBean();
        bean4.setA1("99.99999");
        bean.setInner2(Lists.newArrayList(bean3,bean4));

        RoundTestBean bean5 = new RoundTestBean();
        bean5.setA1("5555.55555");
        RoundTestBean bean6 = new RoundTestBean();
        bean6.setA1("6666.66666");
        bean.setInner3(new RoundTestBean[]{bean5, bean6});

        RoundTestBean bean7 = new RoundTestBean();
        bean7.setA1("777.123");
        Map<String, RoundTestBean> amap = new HashMap<>();
        amap.put("bean7", bean7);
        bean.setInner4(amap);


        Map<String, RoundTestBean[]> amap2 = new HashMap<>();
        RoundTestBean bean8 = new RoundTestBean();
        bean8.setA1("876.156");
        RoundTestBean bean9 = new RoundTestBean();
        bean9.setA1("9876.156");
        amap2.put("beanArrVal", new RoundTestBean[]{bean8, bean9});
        bean.setInner5(amap2);

        RoundTestBean b1 = new RoundTestBean();
        b1.setA1("7788.333");
        RoundTestBean b2 = new RoundTestBean();
        b2.setA1("1122.3344");
        List<RoundTestBean> l = Lists.newArrayList(b1,b2);
        List<List<RoundTestBean>> ll = Lists.newArrayList();
        ll.add(l);
        bean.setInner6(ll);

        RoundOffUtil.that(bean);
        System.err.println();
    }

}