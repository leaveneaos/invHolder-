package com.rjxx.taxeasy.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class Fpcxvo implements Serializable{
	protected Integer kplsh;
	protected Integer djh;
	protected String jylsh;
	protected String fpzlmc;
	protected String fpczlxdm;
	protected String fpczlxmc;
	protected String fpdm;
	protected String fphm;
	protected Integer xfid;
	protected String xfsh;
	protected String xfmc;
	protected Integer gfid;
	protected String gfsh;	
	protected String gfmc;
	protected String hzyfpdm;
	protected String hzyfphm;
	protected String pdfurl;
	protected String lrsj;
	protected String kprq;
	protected String kpr;
	protected Double jshj;
	protected Double hjje;
	protected Double hjse;
	protected String printflag;
	protected String spmc;
	protected String fpzt;
	protected String ddh;
	protected String sfdy;
	protected Integer xcyf;
	protected String hkbz;
	protected String errorReason;
	protected String sqsj;
	protected String ckhkyy;
	protected int sqid;
	protected String ckztbz;
	protected String hkztbz;
	protected String newgfmc;
	public Integer getKplsh() {
		return kplsh;
	}
	public Integer getDjh() {
		return djh;
	}
	public String getJylsh() {
		return jylsh;
	}
	public String getFpzlmc() {
		return fpzlmc;
	}
	public String getFpczlxdm() {
		return fpczlxdm;
	}
	public String getFpdm() {
		return fpdm;
	}
	public String getFphm() {
		return fphm;
	}
	public Integer getXfid() {
		return xfid;
	}
	public String getXfsh() {
		return xfsh;
	}
	public String getXfmc() {
		return xfmc;
	}
	public Integer getGfid() {
		return gfid;
	}
	public String getGfsh() {
		return gfsh;
	}
	public String getGfmc() {
		return gfmc;
	}
	public String getHzyfpdm() {
		return hzyfpdm;
	}
	public String getHzyfphm() {
		return hzyfphm;
	}
	public String getPdfurl() {
		return pdfurl;
	}
	public String getLrsj() {
		return lrsj;
	}
	public Double getJshj() {
		return jshj;
	}
	public Double getHjje() {
		return hjje;
	}
	public Double getHjse() {
		return hjse;
	}
	public String getPrintflag() {
		return printflag;
	}
	public String getSpmc() {
		return spmc;
	}
	public String getFpzt() {
		return fpzt;
	}
	public void setKplsh(Integer kplsh) {
		this.kplsh = kplsh;
	}
	public void setDjh(Integer djh) {
		this.djh = djh;
	}
	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}
	public void setFpzlmc(String fpzlmc) {
		this.fpzlmc = fpzlmc;
	}
	public void setFpczlxdm(String fpczlxdm) {
		this.fpczlxdm = fpczlxdm;
	}
	public void setFpdm(String fpdm) {
		this.fpdm = fpdm;
	}
	public void setFphm(String fphm) {
		this.fphm = fphm;
	}
	public void setXfid(Integer xfid) {
		this.xfid = xfid;
	}
	public void setXfsh(String xfsh) {
		this.xfsh = xfsh;
	}
	public void setXfmc(String xfmc) {
		this.xfmc = xfmc;
	}
	public void setGfid(Integer gfid) {
		this.gfid = gfid;
	}
	public void setGfsh(String gfsh) {
		this.gfsh = gfsh;
	}
	public void setGfmc(String gfmc) {
		this.gfmc = gfmc;
	}
	public void setHzyfpdm(String hzyfpdm) {
		this.hzyfpdm = hzyfpdm;
	}
	public void setHzyfphm(String hzyfphm) {
		this.hzyfphm = hzyfphm;
	}
	public void setPdfurl(String pdfurl) {
		this.pdfurl = pdfurl;
	}
	public void setLrsj(String lrsj) {
		this.lrsj = lrsj;
	}
	public String getKpr() {
		return kpr;
	}
	public void setKpr(String kpr) {
		this.kpr = kpr;
	}
	public void setJshj(Double jshj) {
		this.jshj = jshj;
	}
	public void setHjje(Double hjje) {
		this.hjje = hjje;
	}
	public void setHjse(Double hjse) {
		this.hjse = hjse;
	}
	public void setPrintflag(String printflag) {
		this.printflag = printflag;
	}
	public void setSpmc(String spmc) {
		this.spmc = spmc;
	}
	public void setFpzt(String fpzt) {
		this.fpzt = fpzt;
	}
	public String getDdh() {
		return ddh;
	}
	public void setDdh(String ddh) {
		this.ddh = ddh;
	}
	public String getSfdy() {
		return sfdy;
	}
	public void setSfdy(String sfdy) {
		this.sfdy = sfdy;
	}
	public String getFpczlxmc() {
		return fpczlxmc;
	}
	public void setFpczlxmc(String fpczlxmc) {
		this.fpczlxmc = fpczlxmc;
	}
	public Integer getXcyf() {
		return xcyf;
	}
	public void setXcyf(Integer xcyf) {
		this.xcyf = xcyf;
	}
	public String getHkbz() {
		return hkbz;
	}
	public void setHkbz(String hkbz) {
		this.hkbz = hkbz;
	}
	public String getErrorReason() {
		return errorReason;
	}
	public void setErrorReason(String errorReason) {
		if(errorReason==null){
			this.errorReason= "";
		}else{
			this.errorReason = errorReason;
		}
		
	}
	public String getSqsj() {
		return sqsj;
	}
	public void setSqsj(String sqsj) {
		this.sqsj = sqsj;
	}
	public String getCkhkyy() {
		return ckhkyy;
	}
	public void setCkhkyy(String ckhkyy) {
		if(ckhkyy==null){
			this.ckhkyy = "";
		}else{
			this.ckhkyy = ckhkyy;
		}
		
	}
	public int getSqid() {
		return sqid;
	}
	public void setSqid(int sqid) {
		this.sqid = sqid;
	}
	public String getCkztbz() {
		return ckztbz;
	}
	public void setCkztbz(String ckztbz) {
		this.ckztbz = ckztbz;
	}
	public String getHkztbz() {
		return hkztbz;
	}
	public void setHkztbz(String hkztbz) {
		this.hkztbz = hkztbz;
	}
	public String getNewgfmc() {
		return newgfmc;
	}
	public void setNewgfmc(String newgfmc) {
		this.newgfmc = newgfmc;
	}
	public String getKprq() {
		if(kprq!=null){
			return kprq.substring(0, 10);
		}else{
			return null;
		}
		
	}
	public void setKprq(String kprq) {
		if(kprq!=null){
			this.kprq = kprq.substring(0, 10);
		}else{
			this.kprq = null;
		}
		
	}
	
}
