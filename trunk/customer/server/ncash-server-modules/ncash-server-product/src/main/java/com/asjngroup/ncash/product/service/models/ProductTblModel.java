package com.asjngroup.ncash.product.service.models;

import org.joda.time.DateTime;

public class ProductTblModel
{
	private String prdName;
	private Integer strId;
	private String prdCode;
	private Double prdPrice;
	private Integer prdImgId;
	private String prdDesc;
	private DateTime prdMfgDate;
	private DateTime prdExpiryDate;

	public String getPrdName()
	{
		return prdName;
	}

	public void setPrdName( String prdName )
	{
		this.prdName = prdName;
	}

	public Integer getStrId()
	{
		return strId;
	}

	public void setStrId( Integer strId )
	{
		this.strId = strId;
	}

	public String getPrdCode()
	{
		return prdCode;
	}

	public void setPrdCode( String prdCode )
	{
		this.prdCode = prdCode;
	}

	public Double getPrdPrice()
	{
		return prdPrice;
	}

	public void setPrdPrice( Double prdPrice )
	{
		this.prdPrice = prdPrice;
	}

	public Integer getPrdImgId()
	{
		return prdImgId;
	}

	public void setPrdImgId( Integer prdImgId )
	{
		this.prdImgId = prdImgId;
	}

	public String getPrdDesc()
	{
		return prdDesc;
	}

	public void setPrdDesc( String prdDesc )
	{
		this.prdDesc = prdDesc;
	}

	public DateTime getPrdMfgDate()
	{
		return prdMfgDate;
	}

	public void setPrdMfgDate( DateTime prdMfgDate )
	{
		this.prdMfgDate = prdMfgDate;
	}

	public DateTime getPrdExpiryDate()
	{
		return prdExpiryDate;
	}

	public void setPrdExpiryDate( DateTime prdExpiryDate )
	{
		this.prdExpiryDate = prdExpiryDate;
	}

}
