package com.evmtv.epg.entity;

import com.evmtv.epg.request.BaseRequest;

public class TAdvClassConfig extends BaseRequest {
	/**
	 * @field serialVersionUID
	 * @field_type long
	 */
	private static final long serialVersionUID = 895969386914799369L;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.ID
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Long id;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FPositionId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Integer fpositionid;

	/**
	 * 广告位
	 */
	private String advClassName;

	public String getAdvClassName() {
		return advClassName;
	}

	public void setAdvClassName(String advClassName) {
		this.advClassName = advClassName;
	}

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FBranchId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Long fbranchid;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FDefinition
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private String fdefinition;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FWidth
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Integer fwidth;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FHeight
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Integer fheight;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FSize
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Integer fsize;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FFormat
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private String fformat;

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to
	 * the database column t_adv_class_config.FTime
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	private Double ftime;

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.ID
	 * 
	 * @return the value of t_adv_class_config.ID
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Long getId() {
		return id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.ID
	 * 
	 * @param id
	 *            the value for t_adv_class_config.ID
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FPositionId
	 * 
	 * @return the value of t_adv_class_config.FPositionId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Integer getFpositionid() {
		return fpositionid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FPositionId
	 * 
	 * @param fpositionid
	 *            the value for t_adv_class_config.FPositionId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFpositionid(Integer fpositionid) {
		this.fpositionid = fpositionid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FBranchId
	 * 
	 * @return the value of t_adv_class_config.FBranchId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Long getFbranchid() {
		return fbranchid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FBranchId
	 * 
	 * @param fbranchid
	 *            the value for t_adv_class_config.FBranchId
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFbranchid(Long fbranchid) {
		this.fbranchid = fbranchid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FDefinition
	 * 
	 * @return the value of t_adv_class_config.FDefinition
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public String getFdefinition() {
		return fdefinition;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FDefinition
	 * 
	 * @param fdefinition
	 *            the value for t_adv_class_config.FDefinition
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFdefinition(String fdefinition) {
		this.fdefinition = fdefinition;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FWidth
	 * 
	 * @return the value of t_adv_class_config.FWidth
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Integer getFwidth() {
		return fwidth;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FWidth
	 * 
	 * @param fwidth
	 *            the value for t_adv_class_config.FWidth
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFwidth(Integer fwidth) {
		this.fwidth = fwidth;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FHeight
	 * 
	 * @return the value of t_adv_class_config.FHeight
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Integer getFheight() {
		return fheight;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FHeight
	 * 
	 * @param fheight
	 *            the value for t_adv_class_config.FHeight
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFheight(Integer fheight) {
		this.fheight = fheight;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FSize
	 * 
	 * @return the value of t_adv_class_config.FSize
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Integer getFsize() {
		return fsize;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FSize
	 * 
	 * @param fsize
	 *            the value for t_adv_class_config.FSize
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFsize(Integer fsize) {
		this.fsize = fsize;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FFormat
	 * 
	 * @return the value of t_adv_class_config.FFormat
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public String getFformat() {
		return fformat;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FFormat
	 * 
	 * @param fformat
	 *            the value for t_adv_class_config.FFormat
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFformat(String fformat) {
		this.fformat = fformat;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the
	 * value of the database column t_adv_class_config.FTime
	 * 
	 * @return the value of t_adv_class_config.FTime
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public Double getFtime() {
		return ftime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the
	 * value of the database column t_adv_class_config.FTime
	 * 
	 * @param ftime
	 *            the value for t_adv_class_config.FTime
	 * 
	 * @mbggenerated Wed Aug 21 23:45:14 CST 2013
	 */
	public void setFtime(Double ftime) {
		this.ftime = ftime;
	}
}