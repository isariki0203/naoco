package jp.gr.naoco.alacarte;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.gr.naoco.alacarte.BeanMapper;

public class BeanMapperTest {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparation

    @Before
    public void setup() {
        // nothing to do
    }

    @After
    public void teardown() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Cases

    @Test
    public void test01() {
        Bean01 source = new Bean01();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");

        Bean01 result = BeanMapper.map(source, Bean01.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getLongValue(), result.getLongValue());
        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
    }

    @Test
    public void test02() {
        Bean01 source = new Bean01();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");
        Bean01 target = new Bean01();
        target.setIntValue(789);
        target.setLongValue(777L);
        target.setStrValue("hagehage");

        Bean01 result = BeanMapper.map(source, target);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getLongValue(), result.getLongValue());
        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
    }

    @Test
    public void test03() {
        Bean01 source = new Bean01();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");

        Bean02 result = BeanMapper.map(source, Bean02.class);

        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue().intValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
    }

    @Test
    public void test04() {
        Bean01 sourceElem = new Bean01();
        sourceElem.setIntValue(123);
        sourceElem.setLongValue(456L);
        sourceElem.setStrValue("hogehoge");
        Bean03 source = new Bean03();
        java.util.Date sourceDate = new java.util.Date();
        source.setBean01Value(sourceElem);
        source.setDataValue(sourceDate);

        Bean03 result = BeanMapper.map(source, Bean03.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(sourceDate, result.getDataValue());
        Bean01 resultElem = result.getBean01Value();
        org.junit.Assert.assertFalse(sourceElem == resultElem);
        org.junit.Assert.assertEquals(sourceElem.getLongValue(), resultElem.getLongValue());
        org.junit.Assert.assertEquals(sourceElem.getIntValue(), resultElem.getIntValue());
        org.junit.Assert.assertEquals(sourceElem.getStrValue(), resultElem.getStrValue());
    }

    @Test
    public void test05() {
        Bean01 sourceElem01 = new Bean01();
        sourceElem01.setIntValue(1);
        sourceElem01.setLongValue(2L);
        sourceElem01.setStrValue("A");
        Bean01 sourceElem02 = new Bean01();
        sourceElem02.setIntValue(3);
        sourceElem02.setLongValue(4L);
        sourceElem02.setStrValue("B");
        Bean01 sourceElem03 = new Bean01();
        sourceElem03.setIntValue(5);
        sourceElem03.setLongValue(6L);
        sourceElem03.setStrValue("C");
        Bean04 source = new Bean04();
        source.setBooleanValue(true);
        source.setBeanList(new ArrayList<Bean01>(3));
        source.getBeanList().add(sourceElem01);
        source.getBeanList().add(sourceElem02);
        source.getBeanList().add(sourceElem03);

        Bean04 result = BeanMapper.map(source, Bean04.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.isBooleanValue(), result.isBooleanValue());
        org.junit.Assert.assertFalse(source.getBeanList() == result.getBeanList());
        org.junit.Assert.assertFalse(source.getBeanList().get(0) == result.getBeanList().get(0));
        org.junit.Assert.assertEquals(source.getBeanList().get(0).getLongValue(), //
                result.getBeanList().get(0).getLongValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(0).getStrValue(), //
                result.getBeanList().get(0).getStrValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(0).getIntValue(), //
                result.getBeanList().get(0).getIntValue());
        org.junit.Assert.assertFalse(source.getBeanList().get(1) == result.getBeanList().get(1));
        org.junit.Assert.assertEquals(source.getBeanList().get(1).getLongValue(), //
                result.getBeanList().get(1).getLongValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(1).getStrValue(), //
                result.getBeanList().get(1).getStrValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(1).getIntValue(), //
                result.getBeanList().get(1).getIntValue());
        org.junit.Assert.assertFalse(source.getBeanList().get(2) == result.getBeanList().get(2));
        org.junit.Assert.assertEquals(source.getBeanList().get(2).getLongValue(), //
                result.getBeanList().get(2).getLongValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(2).getStrValue(), //
                result.getBeanList().get(2).getStrValue());
        org.junit.Assert.assertEquals(source.getBeanList().get(2).getIntValue(), //
                result.getBeanList().get(2).getIntValue());
    }

    @Test
    public void test06() {
        Bean01 sourceElem01 = new Bean01();
        sourceElem01.setIntValue(1);
        sourceElem01.setLongValue(2L);
        sourceElem01.setStrValue("A");
        Bean01 sourceElem02 = new Bean01();
        sourceElem02.setIntValue(3);
        sourceElem02.setLongValue(4L);
        sourceElem02.setStrValue("B");
        Bean01 sourceElem03 = new Bean01();
        sourceElem03.setIntValue(5);
        sourceElem03.setLongValue(6L);
        sourceElem03.setStrValue("C");
        Bean05 source = new Bean05();
        source.setBooleanValue(true);
        source.setBeanMap(new HashMap<String, Bean01>());
        source.getBeanMap().put("D", sourceElem01);
        source.getBeanMap().put("E", sourceElem02);
        source.getBeanMap().put("F", sourceElem03);

        Bean05 result = BeanMapper.map(source, Bean05.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getBooleanValue(), result.getBooleanValue());
        org.junit.Assert.assertFalse(source.getBeanMap() == result.getBeanMap());
        org.junit.Assert.assertFalse(source.getBeanMap().get("D") == result.getBeanMap().get("D"));
        org.junit.Assert.assertEquals(source.getBeanMap().get("D").getLongValue(), //
                result.getBeanMap().get("D").getLongValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("D").getStrValue(), //
                result.getBeanMap().get("D").getStrValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("D").getIntValue(), //
                result.getBeanMap().get("D").getIntValue());
        org.junit.Assert.assertFalse(source.getBeanMap().get("E") == result.getBeanMap().get("E"));
        org.junit.Assert.assertEquals(source.getBeanMap().get("E").getLongValue(), //
                result.getBeanMap().get("E").getLongValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("E").getStrValue(), //
                result.getBeanMap().get("E").getStrValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("E").getIntValue(), //
                result.getBeanMap().get("E").getIntValue());
        org.junit.Assert.assertFalse(source.getBeanMap().get("F") == result.getBeanMap().get("F"));
        org.junit.Assert.assertEquals(source.getBeanMap().get("F").getLongValue(), //
                result.getBeanMap().get("F").getLongValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("F").getStrValue(), //
                result.getBeanMap().get("F").getStrValue());
        org.junit.Assert.assertEquals(source.getBeanMap().get("F").getIntValue(), //
                result.getBeanMap().get("F").getIntValue());
    }

    @Test
    public void test07() {
        Bean01 sourceElem01 = new Bean01();
        sourceElem01.setIntValue(1);
        sourceElem01.setLongValue(2L);
        sourceElem01.setStrValue("A");
        Bean01 sourceElem02 = new Bean01();
        sourceElem02.setIntValue(3);
        sourceElem02.setLongValue(4L);
        sourceElem02.setStrValue("B");
        Bean01 sourceElem03 = new Bean01();
        sourceElem03.setIntValue(5);
        sourceElem03.setLongValue(6L);
        sourceElem03.setStrValue("C");
        Bean06 source = new Bean06();
        source.setBooleanValue(true);
        Bean01[] sourceBeans = new Bean01[3];
        sourceBeans[0] = sourceElem01;
        sourceBeans[1] = sourceElem02;
        sourceBeans[2] = sourceElem03;
        source.setBeans(sourceBeans);

        Bean06 result = BeanMapper.map(source, Bean06.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getBooleanValue(), result.getBooleanValue());
        org.junit.Assert.assertFalse(source.getBeans() == result.getBeans());
        org.junit.Assert.assertFalse(source.getBeans()[0] == result.getBeans()[0]);
        org.junit.Assert.assertEquals(source.getBeans()[0].getLongValue(), //
                result.getBeans()[0].getLongValue());
        org.junit.Assert.assertEquals(source.getBeans()[0].getStrValue(), //
                result.getBeans()[0].getStrValue());
        org.junit.Assert.assertEquals(source.getBeans()[0].getIntValue(), //
                result.getBeans()[0].getIntValue());
        org.junit.Assert.assertFalse(source.getBeans()[1] == result.getBeans()[1]);
        org.junit.Assert.assertEquals(source.getBeans()[1].getLongValue(), //
                result.getBeans()[1].getLongValue());
        org.junit.Assert.assertEquals(source.getBeans()[1].getStrValue(), //
                result.getBeans()[1].getStrValue());
        org.junit.Assert.assertEquals(source.getBeans()[1].getIntValue(), //
                result.getBeans()[1].getIntValue());
        org.junit.Assert.assertFalse(source.getBeans()[2] == result.getBeans()[2]);
        org.junit.Assert.assertEquals(source.getBeans()[2].getLongValue(), //
                result.getBeans()[2].getLongValue());
        org.junit.Assert.assertEquals(source.getBeans()[2].getStrValue(), //
                result.getBeans()[2].getStrValue());
        org.junit.Assert.assertEquals(source.getBeans()[2].getIntValue(), //
                result.getBeans()[2].getIntValue());
    }

    @Test
    public void test08() {
        Bean01 sourceElem01 = new Bean01();
        sourceElem01.setIntValue(1);
        sourceElem01.setLongValue(2L);
        sourceElem01.setStrValue("A");
        Bean01 sourceElem02 = new Bean01();
        sourceElem02.setIntValue(3);
        sourceElem02.setLongValue(4L);
        sourceElem02.setStrValue("B");
        Bean01 sourceElem03 = new Bean01();
        sourceElem03.setIntValue(5);
        sourceElem03.setLongValue(6L);
        sourceElem03.setStrValue("C");
        Bean04 source = new Bean04();
        source.setBooleanValue(true);
        source.setBeanList(new ArrayList<Bean01>(3));
        source.getBeanList().add(sourceElem01);
        source.getBeanList().add(sourceElem02);
        source.getBeanList().add(sourceElem03);

        Bean07 result = BeanMapper.map(source, Bean07.class);

        org.junit.Assert.assertEquals(source.isBooleanValue(), result.isBooleanValue());
        // リフレクションではジェネリックの値までは見れないため、
        // コピー先のオブジェクトのジェネリックが異なることを検知できず、
        // 要素の型が異なってもコピーしてしまう。が、容認。
        org.junit.Assert.assertFalse(null == result.getBeanList());
    }

    @Test
    public void test09() {
        Bean01 sourceElem01 = new Bean01();
        sourceElem01.setIntValue(1);
        sourceElem01.setLongValue(2L);
        sourceElem01.setStrValue("A");
        Bean01 sourceElem02 = new Bean01();
        sourceElem02.setIntValue(3);
        sourceElem02.setLongValue(4L);
        sourceElem02.setStrValue("B");
        Bean01 sourceElem03 = new Bean01();
        sourceElem03.setIntValue(5);
        sourceElem03.setLongValue(6L);
        sourceElem03.setStrValue("C");
        Bean06 source = new Bean06();
        source.setBooleanValue(true);
        Bean01[] sourceBeans = new Bean01[3];
        sourceBeans[0] = sourceElem01;
        sourceBeans[1] = sourceElem02;
        sourceBeans[2] = sourceElem03;
        source.setBeans(sourceBeans);

        Bean08 result = BeanMapper.map(source, Bean08.class);

        org.junit.Assert.assertEquals(source.getBooleanValue(), result.getBooleanValue());
        org.junit.Assert.assertTrue(null == result.getBeans());
    }

    @Test
    public void test10() {
        Bean09 source = new Bean09();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");
        source.setDoubleValue(10.0D);
        source.setBigDecimalValue(new BigDecimal(890));

        Bean09 result = BeanMapper.map(source, Bean09.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getLongValue(), result.getLongValue());
        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
        org.junit.Assert.assertEquals(source.getDoubleValue(), result.getDoubleValue());
        org.junit.Assert.assertEquals(source.getBigDecimalValue(), result.getBigDecimalValue());
    }

    @Test
    public void test11() {
        Bean09 source = new Bean09();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");
        source.setDoubleValue(10.0D);
        source.setBigDecimalValue(new BigDecimal(890));

        Bean01 result = BeanMapper.map(source, Bean01.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getLongValue(), result.getLongValue());
        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
        org.junit.Assert.assertNotEquals(source.getClass(), result.getClass());
    }

    @Test
    public void test12() {
        Bean01 source = new Bean01();
        source.setIntValue(123);
        source.setLongValue(456L);
        source.setStrValue("hogehoge");

        Bean09 result = BeanMapper.map(source, Bean09.class);

        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertEquals(source.getLongValue(), result.getLongValue());
        org.junit.Assert.assertEquals(source.getIntValue(), result.getIntValue());
        org.junit.Assert.assertEquals(source.getStrValue(), result.getStrValue());
        org.junit.Assert.assertNotEquals(source.getClass(), result.getClass());
    }

    @Test
    public void test13() {
        Bean10 source = new Bean10();
        byte[] bytes = new byte[1025];
        for (int i = 0; i < 1025; i++) {
            bytes[i] = (byte) (i % 128);
        }
        source.setBytes(bytes);

        Bean10 result = BeanMapper.map(source, Bean10.class);
        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertTrue(source.getBytes() == result.getBytes());
    }

    @Test
    public void test14() {
        Bean10 source = new Bean10();
        byte[] bytes = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            bytes[i] = (byte) (i % 128);
        }
        source.setBytes(bytes);

        Bean10 result = BeanMapper.map(source, Bean10.class);
        org.junit.Assert.assertFalse(source == result);
        org.junit.Assert.assertFalse(source.getBytes() == result.getBytes());
    }

    @Test
    public void test99() {
        Bean99 source = new Bean99();
        for (int i = 0; i < 1000; i++) {
            Bean99 result = BeanMapper.map(source, Bean99.class);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // InnerClasses

    public static class Bean01 implements Serializable {
        private int intValue;
        private long longValue;
        private String strValue;

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }

    public static class Bean02 implements Serializable {
        private Integer intValue;
        private Double doubleValue;
        private String strValue;

        public Integer getIntValue() {
            return intValue;
        }

        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }

        public Double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }

    public static class Bean03 implements Serializable {
        private java.util.Date dataValue;
        private Bean01 bean01Value;

        public java.util.Date getDataValue() {
            return dataValue;
        }

        public void setDataValue(java.util.Date dataValue) {
            this.dataValue = dataValue;
        }

        public Bean01 getBean01Value() {
            return bean01Value;
        }

        public void setBean01Value(Bean01 bean01Value) {
            this.bean01Value = bean01Value;
        }
    }

    public static class Bean04 implements Serializable {
        private boolean booleanValue;
        private List<Bean01> beanList;

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public List<Bean01> getBeanList() {
            return beanList;
        }

        public void setBeanList(List<Bean01> beanList) {
            this.beanList = beanList;
        }
    }

    public static class Bean05 implements Serializable {
        private Boolean booleanValue;
        private Map<String, Bean01> beanMap;

        public Boolean getBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public Map<String, Bean01> getBeanMap() {
            return beanMap;
        }

        public void setBeanMap(Map<String, Bean01> beanMap) {
            this.beanMap = beanMap;
        }
    }

    public static class Bean06 implements Serializable {
        private Boolean booleanValue;
        private Bean01[] beans;

        public Boolean getBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public Bean01[] getBeans() {
            return beans;
        }

        public void setBeans(Bean01[] beans) {
            this.beans = beans;
        }
    }

    public static class Bean07 implements Serializable {
        private boolean booleanValue;
        private List<Bean02> beanList;

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public List<Bean02> getBeanList() {
            return beanList;
        }

        public void setBeanList(List<Bean02> beanList) {
            this.beanList = beanList;
        }
    }

    public static class Bean08 implements Serializable {
        private Boolean booleanValue;
        private Bean02[] beans;

        public Boolean getBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public Bean02[] getBeans() {
            return beans;
        }

        public void setBeans(Bean02[] beans) {
            this.beans = beans;
        }
    }

    public static class Bean09 extends Bean01 {
        private Double doubleValue;
        private BigDecimal bigDecimalValue;

        public Double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public BigDecimal getBigDecimalValue() {
            return bigDecimalValue;
        }

        public void setBigDecimalValue(BigDecimal bigDecimalValue) {
            this.bigDecimalValue = bigDecimalValue;
        }
    }

    public static class Bean10 implements Serializable {
        private byte[] bytes;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    public static class Bean99 implements Serializable {
        private String value01 = "value01";
        private String value02 = "value02";
        private String value03 = "value03";
        private String value04 = "value04";
        private String value05 = "value05";
        private String value06 = "value06";
        private String value07 = "value07";
        private String value08 = "value08";
        private String value09 = "value09";
        private String value10 = "value10";
        private String value11 = "value11";
        private String value12 = "value12";
        private String value13 = "value13";
        private String value14 = "value14";
        private String value15 = "value15";
        private String value16 = "value16";
        private String value17 = "value17";
        private String value18 = "value18";
        private String value19 = "value19";
        private String value20 = "value20";

        public String getValue01() {
            return value01;
        }

        public void setValue01(String value01) {
            this.value01 = value01;
        }

        public String getValue02() {
            return value02;
        }

        public void setValue02(String value02) {
            this.value02 = value02;
        }

        public String getValue03() {
            return value03;
        }

        public void setValue03(String value03) {
            this.value03 = value03;
        }

        public String getValue04() {
            return value04;
        }

        public void setValue04(String value04) {
            this.value04 = value04;
        }

        public String getValue05() {
            return value05;
        }

        public void setValue05(String value05) {
            this.value05 = value05;
        }

        public String getValue06() {
            return value06;
        }

        public void setValue06(String value06) {
            this.value06 = value06;
        }

        public String getValue07() {
            return value07;
        }

        public void setValue07(String value07) {
            this.value07 = value07;
        }

        public String getValue08() {
            return value08;
        }

        public void setValue08(String value08) {
            this.value08 = value08;
        }

        public String getValue09() {
            return value09;
        }

        public void setValue09(String value09) {
            this.value09 = value09;
        }

        public String getValue10() {
            return value10;
        }

        public void setValue10(String value10) {
            this.value10 = value10;
        }

        public String getValue11() {
            return value11;
        }

        public void setValue11(String value11) {
            this.value11 = value11;
        }

        public String getValue12() {
            return value12;
        }

        public void setValue12(String value12) {
            this.value12 = value12;
        }

        public String getValue13() {
            return value13;
        }

        public void setValue13(String value13) {
            this.value13 = value13;
        }

        public String getValue14() {
            return value14;
        }

        public void setValue14(String value14) {
            this.value14 = value14;
        }

        public String getValue15() {
            return value15;
        }

        public void setValue15(String value15) {
            this.value15 = value15;
        }

        public String getValue16() {
            return value16;
        }

        public void setValue16(String value16) {
            this.value16 = value16;
        }

        public String getValue17() {
            return value17;
        }

        public void setValue17(String value17) {
            this.value17 = value17;
        }

        public String getValue18() {
            return value18;
        }

        public void setValue18(String value18) {
            this.value18 = value18;
        }

        public String getValue19() {
            return value19;
        }

        public void setValue19(String value19) {
            this.value19 = value19;
        }

        public String getValue20() {
            return value20;
        }

        public void setValue20(String value20) {
            this.value20 = value20;
        }
    }
}
