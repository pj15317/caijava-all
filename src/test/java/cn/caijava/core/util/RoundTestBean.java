package cn.caijava.core.util;

import cn.caijava.core.util.annotation.RoundOff;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class RoundTestBean {

    @RoundOff
    private String a1;
    @RoundOff
    private String a2;
    @RoundOff
    private float b;
    @RoundOff
    private Float c;
    @RoundOff
    private double d;
    @RoundOff
    private Double e;
    @RoundOff
    private BigDecimal f;

    @RoundOff
    private RoundTestBean inner;

    @RoundOff
    private List<RoundTestBean> inner2;

    @RoundOff
    private RoundTestBean[] inner3;

    @RoundOff
    private Map<String, RoundTestBean> inner4;

    @RoundOff
    private Map<String, RoundTestBean[]> inner5;

    @RoundOff
    private List<List<RoundTestBean>> inner6;

    @RoundOff
    private List<RoundTestBean[]> inner7;

}
