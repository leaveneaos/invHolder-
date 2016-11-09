package com.rjxx.taxeasy.vo;

import java.io.Serializable;
import java.util.Date;

public class FpjVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Integer djh;
	
	protected Date jylssj;
	
	protected String gfmc;
	
	protected Double jshj;
	
	protected String ztbz;
	
	protected String kprq;

	public Integer getDjh() {
		return djh;
	}

	public void setDjh(Integer djh) {
		this.djh = djh;
	}

	public String getGfmc() {
		return gfmc;
	}

	public void setGfmc(String gfmc) {
		this.gfmc = gfmc;
	}

	public Double getJshj() {
		return jshj;
	}

	public void setJshj(Double jshj) {
		this.jshj = jshj;
	}

	public String getZtbz() {
		return ztbz;
	}

	public void setZtbz(String ztbz) {
		this.ztbz = ztbz;
	}

	public Date getJylssj() {
		return jylssj;
	}

	public void setJylssj(Date jylssj) {
		this.jylssj = jylssj;
	}

	public String getKprq() {
		return kprq;
	}

	public void setKprq(String kprq) {
		this.kprq = kprq;
	}
}
